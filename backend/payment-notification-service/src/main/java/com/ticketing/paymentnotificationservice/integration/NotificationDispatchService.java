package com.ticketing.paymentnotificationservice.integration;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.ticketing.paymentnotificationservice.config.IntegrationProperties;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDispatchService {

    private final IntegrationProperties integrationProperties;

    public void sendEmail(String toAddress, String subject, String plainTextBody) throws IOException {
        if (!integrationProperties.sendgridEnabled()) {
            return;
        }
        Email from = new Email(
                integrationProperties.getSendgrid().getFromEmail(),
                integrationProperties.getSendgrid().getFromName());
        Email to = new Email(toAddress);
        Content text = new Content("text/plain", plainTextBody);
        Mail mail = new Mail(from, subject, to, text);

        SendGrid sg = new SendGrid(integrationProperties.getSendgrid().getApiKey());
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        Response response = sg.api(request);
        if (response.getStatusCode() >= 300) {
            throw new IOException("SendGrid HTTP " + response.getStatusCode() + ": " + response.getBody());
        }
    }

    public void sendSms(String toE164, String body) {
        if (!integrationProperties.twilioSmsEnabled()) {
            return;
        }
        if (toE164 == null || toE164.isBlank()) {
            return;
        }
        Message.creator(
                new PhoneNumber(toE164.trim()),
                new PhoneNumber(integrationProperties.getTwilio().getFromNumber()),
                body)
                .create();
    }
}
