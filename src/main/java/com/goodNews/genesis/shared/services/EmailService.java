package com.goodNews.genesis.shared.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

@Service
@RequiredArgsConstructor
public class EmailService {
	
	private final SesClient sesClient;
	
	@Value("${aws.ses.sender-email}")
	private String remitente;
	
	@Async
	public void sendEmail (String destino, String asunto, String bodyHtml) {
		
		Destination destination = Destination.builder().toAddresses(destino).build();
		Content contentSubject = Content.builder().data(asunto).charset("UTF-8").build();
		Content contentBody = Content.builder().data(bodyHtml).charset("UTF-8").build();
		
		Message message = Message.builder()
				.subject(contentSubject)
				.body(Body.builder().html(contentBody).build())
				.build();
		
		SendEmailRequest request = SendEmailRequest.builder() 
				.source(remitente)
				.destination(destination)
				.message(message)
				.build();
		
		sesClient.sendEmail(request);
		
	}

}
