package org.openmedstack.messaging

class CloudEventHeaders {
    companion object {
        val requestId = "X-OMS-Request-Id";
        val conversationId = "X-OMS-Conversation-Id";
        val initiatorId = "X-OMS-Initiator-Id";
        val faultAddress = "X-OMS-Fault-Address";
        val destinationAddress = "X-OMS-Destination-Address";
        val responseAddress = "reply-to";
        val expiration = "expiration";
    }
}
