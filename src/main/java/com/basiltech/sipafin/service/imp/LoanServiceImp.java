package com.basiltech.sipafin.service.imp;

import com.basiltech.sipafin.dto.LoanDtos;
import com.basiltech.sipafin.mapper.LoanMapper;
import com.basiltech.sipafin.model.*;
import com.basiltech.sipafin.repository.*;
import com.basiltech.sipafin.service.LoanService;
import com.basiltech.sipafin.web.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanServiceImp implements LoanService {

    private final LoanAccountRepository loanAccountRepository;
    private final LoanTransactionRepository loanTransactionRepository;
    private final BranchFloatAccountRepository branchFloatAccountRepository;
    private final BranchFloatTransactionRepository branchFloatTransactionRepository;
    private final InvestorRepository investorRepository;
    private final BranchRepository branchRepository;
    private final LoanMapper loanMapper;

    @Override
    @Transactional
    public LoanDtos.LoanAccountResponse receiveLoan(LoanDtos.ReceiveLoanRequest request) {
        Investor investor = findActiveInvestor(request.investorId());
        Branch branch = findActiveBranch(request.branchId());
        BranchFloatAccount floatAccount = findActiveFloatAccount(request.branchId(), request.currency());
        String transactionGroupId = "TXN-" + java.util.UUID.randomUUID();

        LoanAccount loanAccount = new LoanAccount();
        loanAccount.setInvestor(investor);
        loanAccount.setBranch(branch);
        loanAccount.setCurrency(request.currency());

        BigDecimal interestAmount = request.principalAmount()
                .multiply(request.interestRate())
                .divide(BigDecimal.valueOf(100));

        BigDecimal totalPayable = request.principalAmount().add(interestAmount);

        loanAccount.setPrincipalAmount(request.principalAmount());
        loanAccount.setInterestRate(request.interestRate());
        loanAccount.setInterestAmount(interestAmount);
        loanAccount.setTotalPayable(totalPayable);
        loanAccount.setOutstandingBalance(totalPayable);
        loanAccount.setStatus(LoanStatus.ACTIVE);
        loanAccount.setDateReceived(request.dateReceived());
        loanAccount.setDueDate(request.dueDate());
        loanAccount.setDescription(request.description());
        loanAccount.setActionedBy(request.actionedBy());

        LoanAccount savedLoan = loanAccountRepository.save(loanAccount);

        LoanTransaction loanTransaction = new LoanTransaction();
        loanTransaction.setLoanAccount(savedLoan);
        loanTransaction.setBranch(branch);
        loanTransaction.setCurrency(request.currency());
        loanTransaction.setTransactionType(LoanTransactionType.LOAN_RECEIVED);
        loanTransaction.setAmount(request.principalAmount());
        loanTransaction.setTransactionDate(request.dateReceived());
        loanTransaction.setReference(request.reference());
        loanTransaction.setDescription(request.description());
        loanTransaction.setActionedBy(request.actionedBy());
        loanTransactionRepository.save(loanTransaction);

        BigDecimal balanceAfter = floatAccount.getCurrentBalance().add(request.principalAmount());
        floatAccount.setCurrentBalance(balanceAfter);
        floatAccount.setActionedBy(request.actionedBy());
        branchFloatAccountRepository.save(floatAccount);

        BranchFloatTransaction floatTransaction = new BranchFloatTransaction();
        floatTransaction.setBranchFloatAccount(floatAccount);
        floatTransaction.setBranch(branch);
        floatTransaction.setCurrency(request.currency());
        floatTransaction.setTransactionType(BranchFloatTransactionType.LOAN_RECEIVED);
        floatTransaction.setDirection(MoneyDirection.IN);
        floatTransaction.setAmount(request.principalAmount());
        floatTransaction.setBalanceAfter(balanceAfter);
        floatTransaction.setTransactionDate(request.dateReceived());
        floatTransaction.setTransactionGroupId(transactionGroupId);
        floatTransaction.setStatus(TransactionStatus.POSTED);
        floatTransaction.setReference(request.reference());
        floatTransaction.setDescription("Loan received from investor: " + investor.getFullName());
        floatTransaction.setActionedBy(request.actionedBy());
        branchFloatTransactionRepository.save(floatTransaction);

        return loanMapper.toLoanAccountResponse(savedLoan);
    }

    @Override
    @Transactional
    public LoanDtos.LoanAccountResponse repayLoan(Long loanAccountId, LoanDtos.RepayLoanRequest request) {
        LoanAccount loanAccount = findLoan(loanAccountId);
        BranchFloatAccount floatAccount = findActiveFloatAccount(
                loanAccount.getBranch().getId(),
                loanAccount.getCurrency()
        );
        String transactionGroupId = "TXN-" + java.util.UUID.randomUUID();

        if (loanAccount.getStatus() == LoanStatus.PAID || loanAccount.getStatus() == LoanStatus.CANCELLED) {
            throw new IllegalArgumentException("Loan is not active for repayment");
        }

        if (request.amount().compareTo(loanAccount.getOutstandingBalance()) > 0) {
            throw new IllegalArgumentException("Repayment amount cannot exceed outstanding balance");
        }

        if (floatAccount.getCurrentBalance().compareTo(request.amount()) < 0) {
            throw new IllegalArgumentException("Insufficient branch float balance");
        }

        BigDecimal newOutstanding = loanAccount.getOutstandingBalance().subtract(request.amount());
        loanAccount.setOutstandingBalance(newOutstanding);
        loanAccount.setActionedBy(request.actionedBy());

        if (newOutstanding.compareTo(BigDecimal.ZERO) == 0) {
            loanAccount.setStatus(LoanStatus.PAID);
        } else {
            loanAccount.setStatus(LoanStatus.PARTIALLY_PAID);
        }

        LoanAccount savedLoan = loanAccountRepository.save(loanAccount);

        LoanTransaction loanTransaction = new LoanTransaction();
        loanTransaction.setLoanAccount(savedLoan);
        loanTransaction.setBranch(savedLoan.getBranch());
        loanTransaction.setCurrency(savedLoan.getCurrency());
        loanTransaction.setTransactionType(LoanTransactionType.LOAN_REPAYMENT);
        loanTransaction.setAmount(request.amount());
        loanTransaction.setTransactionDate(request.transactionDate());
        loanTransaction.setReference(request.reference());
        loanTransaction.setDescription(request.description());
        loanTransaction.setActionedBy(request.actionedBy());
        loanTransactionRepository.save(loanTransaction);

        BigDecimal balanceAfter = floatAccount.getCurrentBalance().subtract(request.amount());
        floatAccount.setCurrentBalance(balanceAfter);
        floatAccount.setActionedBy(request.actionedBy());
        branchFloatAccountRepository.save(floatAccount);

        BranchFloatTransaction floatTransaction = new BranchFloatTransaction();
        floatTransaction.setBranchFloatAccount(floatAccount);
        floatTransaction.setBranch(savedLoan.getBranch());
        floatTransaction.setCurrency(savedLoan.getCurrency());
        floatTransaction.setTransactionType(BranchFloatTransactionType.LOAN_REPAYMENT);
        floatTransaction.setDirection(MoneyDirection.OUT);
        floatTransaction.setAmount(request.amount());
        floatTransaction.setBalanceAfter(balanceAfter);
        floatTransaction.setTransactionDate(request.transactionDate());
        floatTransaction.setTransactionGroupId(transactionGroupId);
        floatTransaction.setStatus(TransactionStatus.POSTED);
        floatTransaction.setReference(request.reference());
        floatTransaction.setDescription("Loan repayment to investor: " + savedLoan.getInvestor().getFullName());
        floatTransaction.setActionedBy(request.actionedBy());
        branchFloatTransactionRepository.save(floatTransaction);

        return loanMapper.toLoanAccountResponse(savedLoan);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanDtos.LoanAccountResponse> getAllLoans() {
        return loanAccountRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(LoanAccount::getId).reversed())
                .map(loanMapper::toLoanAccountResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanDtos.LoanAccountResponse> getLoansByInvestor(Long investorId) {
        return loanAccountRepository.findByInvestorId(investorId)
                .stream()
                .sorted(Comparator.comparing(LoanAccount::getId).reversed())
                .map(loanMapper::toLoanAccountResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanDtos.LoanAccountResponse> getLoansByBranch(Long branchId) {
        return loanAccountRepository.findByBranchId(branchId)
                .stream()
                .sorted(Comparator.comparing(LoanAccount::getId).reversed())
                .map(loanMapper::toLoanAccountResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanDtos.LoanAccountResponse> getLoansByStatus(LoanStatus status) {
        return loanAccountRepository.findByStatus(status)
                .stream()
                .sorted(Comparator.comparing(LoanAccount::getId).reversed())
                .map(loanMapper::toLoanAccountResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanDtos.LoanAccountResponse> searchLoansByInvestorName(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllLoans();
        }

        return loanAccountRepository.findByInvestorFullNameContainingIgnoreCase(keyword)
                .stream()
                .sorted(Comparator.comparing(LoanAccount::getId).reversed())
                .map(loanMapper::toLoanAccountResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LoanDtos.LoanAccountResponse getLoanById(Long id) {
        return loanMapper.toLoanAccountResponse(findLoan(id));
    }

    private BranchFloatAccount findActiveFloatAccount(Long branchId, CurrencyCode currency) {
        BranchFloatAccount floatAccount = branchFloatAccountRepository.findByBranchIdAndCurrency(branchId, currency)
                .orElseThrow(() -> new NotFoundException("Branch float account not found for selected branch and currency"));

        if (!floatAccount.isActive()) {
            throw new IllegalArgumentException("Branch float account is inactive");
        }

        if (!floatAccount.getBranch().isActive()) {
            throw new IllegalArgumentException("Branch is inactive");
        }

        return floatAccount;
    }

    @Override
    @Transactional
    public LoanDtos.LoanAccountResponse updateLoan(Long id, LoanDtos.UpdateLoanRequest request) {
        LoanAccount loanAccount = findLoan(id);
        loanAccount.setDueDate(request.dueDate());
        loanAccount.setDescription(request.description());
        loanAccount.setActionedBy(request.actionedBy());
        return loanMapper.toLoanAccountResponse(loanAccountRepository.save(loanAccount));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanDtos.LoanTransactionResponse> getLoanTransactions(Long loanAccountId) {
        return loanTransactionRepository.findByLoanAccountId(loanAccountId)
                .stream()
                .sorted(Comparator.comparing(LoanTransaction::getId).reversed())
                .map(loanMapper::toLoanTransactionResponse)
                .toList();
    }

    private LoanAccount findLoan(Long id) {
        return loanAccountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Loan account not found"));
    }

    private Investor findActiveInvestor(Long id) {
        Investor investor = investorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Investor not found"));

        if (!investor.isActive()) {
            throw new IllegalArgumentException("Investor is inactive");
        }

        return investor;
    }

    private Branch findActiveBranch(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Branch not found"));

        if (!branch.isActive()) {
            throw new IllegalArgumentException("Branch is inactive");
        }

        return branch;
    }
}