package com.picpaydesafio.service;

import com.picpaydesafio.domain.transaction.Transaction;
import com.picpaydesafio.domain.user.User;
import com.picpaydesafio.dtos.TransactionDTO;
import com.picpaydesafio.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class TransactionService {
    @Autowired
    private UserService userService;

    @Autowired
    private TransactionRepository repository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private NotificationService notificationService;

    public Transaction createTransaction(TransactionDTO transaction) throws Exception {

        User sender = this.userService.findUserById(transaction.senderId());
        User receiver = this.userService.findUserById(transaction.receiverId());

        userService.validateTransaction(sender, transaction.value());

        boolean isAuthorized = this.authorizeTransaction(sender, transaction.value());

        if (!isAuthorized){
            throw new Exception("Transação não autorizada!");
        }

        Transaction newtransaction = new Transaction();
        newtransaction.setAmount(transaction.value());
        newtransaction.setReceiver(receiver);
        newtransaction.setSender(sender);
        newtransaction.setTimestamp(LocalDateTime.now());

        sender.setBalance(sender.getBalance().subtract(transaction.value()));
        receiver.setBalance(receiver.getBalance().add(transaction.value()));

        this.repository.save(newtransaction);
        this.userService.saveUser(sender);
        this.userService.saveUser(receiver);

        this.notificationService.sendNotification(sender, "Transação realizada com sucesso!");
        this.notificationService.sendNotification(receiver, "Transação recebida com sucesso!");

        return newtransaction;
    }

    public boolean authorizeTransaction(User sender, BigDecimal value){

        ResponseEntity<Map> authorizeResponse = restTemplate.getForEntity("https://run.mocky.io/v3/5794d450-d2e2-4412-8131-73d0293ac1cc", Map.class);

        if (authorizeResponse.getStatusCode() == HttpStatus.OK){
            String message = (String) authorizeResponse.getBody().get("message");
            return "Autorizado".equalsIgnoreCase(message);
        }else return false;

    }


}
