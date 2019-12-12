package com.nastel.bank;

import java.util.Enumeration;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.log4j.Logger;

public class MsgListener implements MessageListener {
	private static Logger logger = Logger.getLogger(MsgListener.class);

	private Thread thd;
	private int msgsReceived = 0;
	private int msgsExpected;

	public MsgListener(Thread thd, int msgsExpected) {
		this.thd = thd;
		this.msgsExpected = msgsExpected;
	}

	public int getMsgsReceived() {
		return msgsReceived;
	}

	public void onMessage(Message rspMsg) {
		msgsReceived++;

		try {
			if (rspMsg != null)
			{
				logger.info("\t-> received reply (" + msgsReceived + " of " + msgsExpected + ")");

				if (logger.isDebugEnabled()) {
					logger.debug("\t  message class=" + rspMsg.getClass().getName());
					logger.debug("\t  msgId=" + rspMsg.getJMSMessageID());
					logger.debug("\t  correlId="+ rspMsg.getJMSCorrelationID());

					Enumeration<?> props = rspMsg.getPropertyNames();
					while (props.hasMoreElements()) {
						String prop = (String)props.nextElement();
						logger.debug("\t  " + prop + "=" + rspMsg.getObjectProperty(prop).toString());
					}

					logger.trace("\treplyMsg=" + rspMsg);
				}
			}
			else
			{
				logger.info("!! NULL message received (" + msgsReceived + " of " + msgsExpected + ")");
			}
		}
		catch (JMSException e) {
			logger.error("Error processing message", e);
		}

		if (thd != null)
			thd.interrupt();
	}

}
