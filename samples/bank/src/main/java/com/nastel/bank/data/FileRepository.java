package com.nastel.bank.data;

import java.util.ArrayList;
import java.util.Iterator;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueSession;
import javax.jms.TextMessage;

public class FileRepository extends ArrayList<String> implements JmsMessageRepository {

	private static final long serialVersionUID = 1L;
	//private static Logger logger = Logger.getLogger(FileMessages.class.getName());        
	protected Iterator<String> iter;

	public FileRepository(String fileData, int numTrans)
	{
		super();			

		for(int i = 0; i < numTrans; i++)
			add(fileData);

		iter = iterator();
	}

	public boolean hasNext()
	{
		return iter.hasNext();
	}

	public Message next(QueueSession queueSession) throws JMSException 
	{
		String text = null;

		if (iter.hasNext()) 
			text = (String)iter.next();
		else
			return null;

		TextMessage message = queueSession.createTextMessage();
		message.setText(text);

		return message;
	}
}
