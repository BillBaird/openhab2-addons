package org.openhab.binding.pentair.easytouch.internal;

public class PendingResponse {

    private byte[] m_sentBytes;
    private long m_sentMillis;
    private Message m_expectedResponse;

    public PendingResponse(byte[] sentBytes, Message expectedResponse) {
        m_sentBytes = sentBytes;
        m_sentMillis = System.currentTimeMillis();
        m_expectedResponse = expectedResponse;
    }

    public boolean Acknowledged(Message responseMsg) {
        return m_expectedResponse.matches(responseMsg);
    }

    public boolean IsDupOf(PendingResponse other) {
        byte[] otherSentBytes = other.getSentBytes();
        if (m_sentBytes.length != otherSentBytes.length) {
            return false;
        }
        boolean result = true;
        for (int i = m_sentBytes.length - 1; i >= 0; i--) {
            result = result && m_sentBytes[i] == otherSentBytes[i];
        }
        return result && m_expectedResponse.matches(other.getExpectedResponse());
    }

    public long getSentMillis() {
        return m_sentMillis;
    }

    public byte[] getSentBytes() {
        return m_sentBytes;
    }

    public Message getExpectedResponse() {
        return m_expectedResponse;
    }

    public void resetTimer() {
        m_sentMillis = System.currentTimeMillis();
    }

}
