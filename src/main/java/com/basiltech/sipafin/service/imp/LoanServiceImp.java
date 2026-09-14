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
    private final PettyCashTransactionRepository pettyCashTransactionRepository;
    private final InvestorRepository investorRepository;
    private final BranchRepository branchRepository;
    private final LoanMapper loanMapper;

    @Override
    @Transactional
    public LoanDtos.LoanAccountResponse receiveLoan(LoanDtos.ReceiveLoanRequest request) {
        Investor investor = findActiveInvestor(request.investorId());
        Branch branch = findActiveBranch(request.branchId());

        LoanAccount loanAccount = new LoanAccount();
        loanAccount.setInvestor(investor);
        loanAccount.setBranch(branch);
        loanAccount.setPrincipalAmount(request.principalAmount());
        loanAccount.setOutstandingBalance(request.principalAmount());
        loanAccount.setStatus(LoanStatus.ACTIVE);
        loanAccount.setDateReceived(request.dateReceived());
        loanAccount.setDueDate(request.dueDate());
        loanAccount.setDescription(request.description());
        loanAccount.setActionedBy(request.actionedBy());

        LoanAccount savedLoan = loanAccountRepository.save(loanAccount);

        LoanTransaction loanTransaction = new LoanTransaction();
        loanTransaction.setLoanAccount(savedLoan);
        loanTransaction.setBranch(branch);
        loanTransaction.setTransactionType(LoanTransactionType.LOAN_RECEIVED);
        loanTransaction.setAmount(request.principalAmount());
        loanTransaction.setTransactionDate(request.dateReceived());
        loanTransaction.setReference(request.reference());
        loanTransaction.setDescription(request.description());
        loanTransaction.setActionedBy(request.actionedBy());
        loanTransactionRepository.save(loanTransaction);

        PettyCashTransaction pettyCashTransaction = new PettyCashTransaction();
        pettyCashTransaction.setBranch(branch);
        pettyCashTransaction.setTransactionType(PettyCashTransactionType.LOAN_RECEIVED);
        pettyCashTransaction.setDirection(CashDirection.IN);
        pettyCashTransaction.setAmount(request.principalAmount());
        pettyCashTransaction.setTransactionDate(request.dateReceived());
        pettyCashTransaction.setTransactionGroupId("TXN-" + java.util.UUID.randomUUID());
        pettyCashTransaction.setStatus(TransactionStatus.POSTED);
        pettyCashTransaction.setReference(request.reference());
        pettyCashTransaction.setDescription("Loan received from investor: " + investor.getFullName());
        pettyCashTransaction.setActionedBy(request.actionedBy());
        pettyCashTransactionRepository.save(pettyCashTransaction);

        branch.setCurrentCashFloatBalance(branch.getCurrentCashFloatBalance().add(request.principalAmount()));
        branch.setActionedBy(request.actionedBy());
        branchRepository.save(branch);

        return loanMapper.toLoanAccountResponse(savedLoan);
    }

    @Override
    @Transactional
    public LoanDtos.LoanAccountResponse repayLoan(Long loanAccountId, LoanDtos.RepayLoanRequest request) {
        LoanAccount loanAccount = findLoan(loanAccountId);

        if (loanAccount.getStatus() == LoanStatus.PAID || loanAccount.getStatus() == LoanStatus.CANCELLED) {
            throw new IllegalArgumentException("Loan is not active for repayment");
        }

        if (request.amount().compareTo(loanAccount.getOutstandingBalance()) > 0) {
            throw new IllegalArgumentException("Repayment amount cannot exceed outstanding balance");
        }

        if (loanAccount.getBranch().getCurrentCashFloatBalance().compareTo(request.amount()) < 0) {
            throw new IllegalArgumentException("Insufficient branch cash float balance");
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
        loanTransaction.setTransactionType(LoanTransactionType.LOAN_REPAYMENT);
        loanTransaction.setAmount(request.amount());
        loanTransaction.setTransactionDate(request.transactionDate());
        loanTransaction.setReference(request.reference());
        loanTransaction.setDescription(request.description());
        loanTransaction.setActionedBy(request.actionedBy());
        loanTransactionRepository.save(loanTransaction);

        PettyCashTransaction pettyCashTransaction = new PettyCashTransaction();
        pettyCashTransaction.setBranch(savedLoan.getBranch());
        pettyCashTransaction.setTransactionType(PettyCashTransactionType.LOAN_REPAYMENT);
        pettyCashTransaction.setDirection(CashDirection.OUT);
        pettyCashTransaction.setAmount(request.amount());
        pettyCashTransaction.setTransactionDate(request.transactionDate());
        pettyCashTransaction.setTransactionGroupId("TXN-" + java.util.UUID.randomUUID());
        pettyCashTransaction.setStatus(TransactionStatus.POSTED);
        pettyCashTransaction.setReference(request.reference());
        pettyCashTransaction.setDescription("Loan repayment to investor: " + savedLoan.getInvestor().getFullName());
        pettyCashTransaction.setActionedBy(request.actionedBy());
        pettyCashTransactionRepository.save(pettyCashTransaction);

        Branch branch = savedLoan.getBranch();
        branch.setCurrentCashFloatBalance(branch.getCurrentCashFloatBalance().subtract(request.amount()));
        branch.setActionedBy(request.actionedBy());
        branchRepository.save(branch);

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