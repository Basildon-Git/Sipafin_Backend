package com.basiltech.sipafin.service.imp;

import com.basiltech.sipafin.dto.ClientLoanDtos;
import com.basiltech.sipafin.mapper.ClientLoanMapper;
import com.basiltech.sipafin.model.*;
import com.basiltech.sipafin.repository.*;
import com.basiltech.sipafin.service.ClientLoanService;
import com.basiltech.sipafin.web.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientLoanServiceImp implements ClientLoanService {

    private final ClientLoanAccountRepository clientLoanAccountRepository;
    private final ClientLoanTransactionRepository clientLoanTransactionRepository;
    private final BranchRepository branchRepository;
    private final BranchFloatAccountRepository branchFloatAccountRepository;
    private final BranchFloatTransactionRepository branchFloatTransactionRepository;
    private final BankAccountRepository bankAccountRepository;
    private final BankTransactionRepository bankTransactionRepository;
    private final ClientLoanMapper clientLoanMapper;

    @Override
    @Transactional
    public ClientLoanDtos.ClientLoanAccountResponse issueClientLoan(ClientLoanDtos.IssueClientLoanRequest request) {
        Branch branch = findActiveBranch(request.branchId());
        String transactionGroupId = generateGroupId();

        BigDecimal interestAmount = request.principalAmount()
                .multiply(request.interestRate())
                .divide(BigDecimal.valueOf(100));

        BigDecimal totalReceivable = request.principalAmount().add(interestAmount);

        BankAccount fundingBankAccount = null;

        if (request.fundingSource() == MoneySourceType.BRANCH_FLOAT) {
            BranchFloatAccount floatAccount = findActiveFloatAccount(request.branchId(), request.currency());
            ensureEnoughFloatBalance(floatAccount, request.principalAmount());

            BigDecimal balanceAfter = floatAccount.getCurrentBalance().subtract(request.principalAmount());
            floatAccount.setCurrentBalance(balanceAfter);
            floatAccount.setActionedBy(request.actionedBy());
            branchFloatAccountRepository.save(floatAccount);

            BranchFloatTransaction floatTransaction = new BranchFloatTransaction();
            floatTransaction.setBranchFloatAccount(floatAccount);
            floatTransaction.setBranch(branch);
            floatTransaction.setCurrency(request.currency());
            floatTransaction.setTransactionType(BranchFloatTransactionType.CLIENT_LOAN_ISSUED);
            floatTransaction.setDirection(MoneyDirection.OUT);
            floatTransaction.setAmount(request.principalAmount());
            floatTransaction.setBalanceAfter(balanceAfter);
            floatTransaction.setTransactionDate(request.dateIssued());
            floatTransaction.setTransactionGroupId(transactionGroupId);
            floatTransaction.setStatus(TransactionStatus.POSTED);
            floatTransaction.setReference(request.reference());
            floatTransaction.setDescription(request.description());
            floatTransaction.setActionedBy(request.actionedBy());
            branchFloatTransactionRepository.save(floatTransaction);
        } else {
            if (request.fundingBankAccountId() == null) {
                throw new IllegalArgumentException("Funding bank account ID is required when funding source is BANK_ACCOUNT");
            }

            fundingBankAccount = findActiveBankAccount(request.fundingBankAccountId(), request.currency());
            ensureEnoughBankBalance(fundingBankAccount, request.principalAmount());

            BigDecimal balanceAfter = fundingBankAccount.getCurrentBalance().subtract(request.principalAmount());
            fundingBankAccount.setCurrentBalance(balanceAfter);
            fundingBankAccount.setActionedBy(request.actionedBy());
            bankAccountRepository.save(fundingBankAccount);

            BankTransaction bankTransaction = new BankTransaction();
            bankTransaction.setBankAccount(fundingBankAccount);
            bankTransaction.setBranch(branch);
            bankTransaction.setCurrency(request.currency());
            bankTransaction.setTransactionType(BankLedgerTransactionType.CLIENT_LOAN_ISSUED);
            bankTransaction.setDirection(MoneyDirection.OUT);
            bankTransaction.setAmount(request.principalAmount());
            bankTransaction.setBalanceAfter(balanceAfter);
            bankTransaction.setTransactionDate(request.dateIssued());
            bankTransaction.setTransactionGroupId(transactionGroupId);
            bankTransaction.setStatus(TransactionStatus.POSTED);
            bankTransaction.setReference(request.reference());
            bankTransaction.setDescription(request.description());
            bankTransaction.setActionedBy(request.actionedBy());
            bankTransactionRepository.save(bankTransaction);
        }

        ClientLoanAccount account = new ClientLoanAccount();
        account.setBranch(branch);
        account.setClientName(request.clientName().trim());
        account.setClientPhone(request.clientPhone().trim());
        account.setCurrency(request.currency());
        account.setPrincipalAmount(request.principalAmount());
        account.setInterestRate(request.interestRate());
        account.setInterestAmount(interestAmount);
        account.setTotalReceivable(totalReceivable);
        account.setOutstandingBalance(totalReceivable);
        account.setFundingSource(request.fundingSource());
        account.setFundingBankAccount(fundingBankAccount);
        account.setStatus(ClientLoanStatus.ACTIVE);
        account.setDateIssued(request.dateIssued());
        account.setDueDate(request.dueDate());
        account.setDescription(request.description());
        account.setActionedBy(request.actionedBy());

        ClientLoanAccount savedAccount = clientLoanAccountRepository.save(account);

        ClientLoanTransaction transaction = new ClientLoanTransaction();
        transaction.setClientLoanAccount(savedAccount);
        transaction.setBranch(branch);
        transaction.setCurrency(request.currency());
        transaction.setTransactionType(ClientLoanTransactionType.LOAN_ISSUED);
        transaction.setAmount(request.principalAmount());
        transaction.setTransactionDate(request.dateIssued());
        transaction.setTransactionGroupId(transactionGroupId);
        transaction.setReference(request.reference());
        transaction.setDescription(request.description());
        transaction.setActionedBy(request.actionedBy());
        clientLoanTransactionRepository.save(transaction);

        return clientLoanMapper.toAccountResponse(savedAccount);
    }

    @Override
    @Transactional
    public ClientLoanDtos.ClientLoanAccountResponse repayClientLoan(
            Long clientLoanAccountId,
            ClientLoanDtos.RepayClientLoanRequest request
    ) {
        ClientLoanAccount account = findClientLoan(clientLoanAccountId);
        String transactionGroupId = generateGroupId();

        if (account.getStatus() == ClientLoanStatus.PAID || account.getStatus() == ClientLoanStatus.CANCELLED) {
            throw new IllegalArgumentException("Client loan is not active for repayment");
        }

        if (request.amount().compareTo(account.getOutstandingBalance()) > 0) {
            throw new IllegalArgumentException("Repayment amount cannot exceed outstanding balance");
        }

        if (request.paymentDestination() == MoneySourceType.BRANCH_FLOAT) {
            BranchFloatAccount floatAccount = findActiveFloatAccount(account.getBranch().getId(), account.getCurrency());

            BigDecimal balanceAfter = floatAccount.getCurrentBalance().add(request.amount());
            floatAccount.setCurrentBalance(balanceAfter);
            floatAccount.setActionedBy(request.actionedBy());
            branchFloatAccountRepository.save(floatAccount);

            BranchFloatTransaction floatTransaction = new BranchFloatTransaction();
            floatTransaction.setBranchFloatAccount(floatAccount);
            floatTransaction.setBranch(account.getBranch());
            floatTransaction.setCurrency(account.getCurrency());
            floatTransaction.setTransactionType(BranchFloatTransactionType.CLIENT_LOAN_REPAYMENT);
            floatTransaction.setDirection(MoneyDirection.IN);
            floatTransaction.setAmount(request.amount());
            floatTransaction.setBalanceAfter(balanceAfter);
            floatTransaction.setTransactionDate(request.transactionDate());
            floatTransaction.setTransactionGroupId(transactionGroupId);
            floatTransaction.setStatus(TransactionStatus.POSTED);
            floatTransaction.setReference(request.reference());
            floatTransaction.setDescription(request.description());
            floatTransaction.setActionedBy(request.actionedBy());
            branchFloatTransactionRepository.save(floatTransaction);
        } else {
            if (request.bankAccountId() == null) {
                throw new IllegalArgumentException("Bank account ID is required when repayment destination is BANK_ACCOUNT");
            }

            BankAccount bankAccount = findActiveBankAccount(request.bankAccountId(), account.getCurrency());

            BigDecimal balanceAfter = bankAccount.getCurrentBalance().add(request.amount());
            bankAccount.setCurrentBalance(balanceAfter);
            bankAccount.setActionedBy(request.actionedBy());
            bankAccountRepository.save(bankAccount);

            BankTransaction bankTransaction = new BankTransaction();
            bankTransaction.setBankAccount(bankAccount);
            bankTransaction.setBranch(account.getBranch());
            bankTransaction.setCurrency(account.getCurrency());
            bankTransaction.setTransactionType(BankLedgerTransactionType.CLIENT_LOAN_REPAYMENT);
            bankTransaction.setDirection(MoneyDirection.IN);
            bankTransaction.setAmount(request.amount());
            bankTransaction.setBalanceAfter(balanceAfter);
            bankTransaction.setTransactionDate(request.transactionDate());
            bankTransaction.setTransactionGroupId(transactionGroupId);
            bankTransaction.setStatus(TransactionStatus.POSTED);
            bankTransaction.setReference(request.reference());
            bankTransaction.setDescription(request.description());
            bankTransaction.setActionedBy(request.actionedBy());
            bankTransactionRepository.save(bankTransaction);
        }

        BigDecimal newOutstanding = account.getOutstandingBalance().subtract(request.amount());
        account.setOutstandingBalance(newOutstanding);
        account.setActionedBy(request.actionedBy());

        if (newOutstanding.compareTo(BigDecimal.ZERO) == 0) {
            account.setStatus(ClientLoanStatus.PAID);
        } else {
            account.setStatus(ClientLoanStatus.PARTIALLY_PAID);
        }

        ClientLoanAccount savedAccount = clientLoanAccountRepository.save(account);

        ClientLoanTransaction transaction = new ClientLoanTransaction();
        transaction.setClientLoanAccount(savedAccount);
        transaction.setBranch(savedAccount.getBranch());
        transaction.setCurrency(savedAccount.getCurrency());
        transaction.setTransactionType(ClientLoanTransactionType.LOAN_REPAYMENT);
        transaction.setAmount(request.amount());
        transaction.setTransactionDate(request.transactionDate());
        transaction.setTransactionGroupId(transactionGroupId);
        transaction.setReference(request.reference());
        transaction.setDescription(request.description());
        transaction.setActionedBy(request.actionedBy());
        clientLoanTransactionRepository.save(transaction);

        return clientLoanMapper.toAccountResponse(savedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientLoanDtos.ClientLoanAccountResponse> getAllClientLoans() {
        return clientLoanAccountRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(ClientLoanAccount::getId).reversed())
                .map(clientLoanMapper::toAccountResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClientLoanDtos.ClientLoanAccountResponse getClientLoanById(Long id) {
        return clientLoanMapper.toAccountResponse(findClientLoan(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientLoanDtos.ClientLoanAccountResponse> getClientLoansByBranch(Long branchId) {
        return clientLoanAccountRepository.findByBranchId(branchId)
                .stream()
                .sorted(Comparator.comparing(ClientLoanAccount::getId).reversed())
                .map(clientLoanMapper::toAccountResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientLoanDtos.ClientLoanAccountResponse> getClientLoansByBranchAndCurrency(Long branchId, CurrencyCode currency) {
        return clientLoanAccountRepository.findByBranchIdAndCurrency(branchId, currency)
                .stream()
                .sorted(Comparator.comparing(ClientLoanAccount::getId).reversed())
                .map(clientLoanMapper::toAccountResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientLoanDtos.ClientLoanAccountResponse> getClientLoansByStatus(ClientLoanStatus status) {
        return clientLoanAccountRepository.findByStatus(status)
                .stream()
                .sorted(Comparator.comparing(ClientLoanAccount::getId).reversed())
                .map(clientLoanMapper::toAccountResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientLoanDtos.ClientLoanAccountResponse> searchClientLoans(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllClientLoans();
        }

        return clientLoanAccountRepository
                .findByClientNameContainingIgnoreCaseOrClientPhoneContainingIgnoreCase(keyword, keyword)
                .stream()
                .sorted(Comparator.comparing(ClientLoanAccount::getId).reversed())
                .map(clientLoanMapper::toAccountResponse)
                .toList();
    }

    @Override
    @Transactional
    public ClientLoanDtos.ClientLoanAccountResponse updateClientLoan(Long id, ClientLoanDtos.UpdateClientLoanRequest request) {
        ClientLoanAccount account = findClientLoan(id);
        account.setDueDate(request.dueDate());
        account.setDescription(request.description());
        account.setActionedBy(request.actionedBy());
        return clientLoanMapper.toAccountResponse(clientLoanAccountRepository.save(account));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientLoanDtos.ClientLoanTransactionResponse> getClientLoanTransactions(Long clientLoanAccountId) {
        return clientLoanTransactionRepository.findByClientLoanAccountId(clientLoanAccountId)
                .stream()
                .sorted(Comparator.comparing(ClientLoanTransaction::getId).reversed())
                .map(clientLoanMapper::toTransactionResponse)
                .toList();
    }

    private ClientLoanAccount findClientLoan(Long id) {
        return clientLoanAccountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Client loan not found"));
    }

    private Branch findActiveBranch(Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new NotFoundException("Branch not found"));

        if (!branch.isActive()) {
            throw new IllegalArgumentException("Branch is inactive");
        }

        return branch;
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

    private BankAccount findActiveBankAccount(Long bankAccountId, CurrencyCode currency) {
        BankAccount bankAccount = bankAccountRepository.findById(bankAccountId)
                .orElseThrow(() -> new NotFoundException("Bank account not found"));

        if (!bankAccount.isActive()) {
            throw new IllegalArgumentException("Bank account is inactive");
        }

        if (!bankAccount.getCurrency().equals(currency)) {
            throw new IllegalArgumentException("Selected bank account currency does not match transaction currency");
        }

        if (!bankAccount.getBank().isActive()) {
            throw new IllegalArgumentException("Bank is inactive");
        }

        return bankAccount;
    }

    private void ensureEnoughFloatBalance(BranchFloatAccount floatAccount, BigDecimal amount) {
        if (floatAccount.getCurrentBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient branch float balance");
        }
    }

    private void ensureEnoughBankBalance(BankAccount bankAccount, BigDecimal amount) {
        if (bankAccount.getCurrentBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient bank account balance");
        }
    }

    private String generateGroupId() {
        return "CL-" + UUID.randomUUID();
    }
}