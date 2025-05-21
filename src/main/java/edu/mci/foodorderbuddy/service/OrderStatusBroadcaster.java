package edu.mci.foodorderbuddy.service;

import com.vaadin.flow.component.UI;
import edu.mci.foodorderbuddy.data.entity.OrderStatus;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

public class OrderStatusBroadcaster {

    private static final Set<Consumer<OrderStatusMessage>> listeners = new CopyOnWriteArraySet<>();

    public static class OrderStatusMessage {
        public final Long cartId;
        public final OrderStatus newStatus;
        public final String targetUsername;

        public OrderStatusMessage(Long cartId, OrderStatus newStatus, String targetUsername) {
            this.cartId = cartId;
            this.newStatus = newStatus;
            this.targetUsername = targetUsername;
        }
    }

    public static synchronized void register(Consumer<OrderStatusMessage> listener) {
        listeners.add(listener);
    }

    public static synchronized void unregister(Consumer<OrderStatusMessage> listener) {
        listeners.remove(listener);
    }

    public static synchronized void broadcast(OrderStatusMessage message) {
        for (Consumer<OrderStatusMessage> listener : listeners) {
            listener.accept(message);
        }
    }
}
