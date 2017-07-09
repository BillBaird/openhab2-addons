package org.openhab.binding.pentair.easytouch.internal;

public class PendingResponse {

    private Message m_sentCommand;
    private long m_sentMillis;
    private Message m_expectedResponse;

    public PendingResponse(Message command, Message expectedResponse) {
        m_sentCommand = command;
        m_sentMillis = System.currentTimeMillis();
        m_expectedResponse = expectedResponse;
    }

    public boolean Acknowledged(Message responseMsg) {
        return m_expectedResponse.matches(responseMsg);
    }

    public boolean IsDupOf(PendingResponse other) {
        return m_sentCommand.matches(other.getSentCommand()) && m_expectedResponse.matches(other.getExpectedResponse());
    }

    public long getSentMillis() {
        return m_sentMillis;
    }

    public Message getSentCommand() {
        return m_sentCommand;
    }

    public Message getExpectedResponse() {
        return m_expectedResponse;
    }

    public void resetTimer() {
        m_sentMillis = System.currentTimeMillis();
    }

}
