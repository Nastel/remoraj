package com.nastel.bank.data;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueSession;

public interface JmsMessageRepository {

    public boolean hasNext();
    public  Message next(QueueSession session) throws JMSException;
    public int size();
}