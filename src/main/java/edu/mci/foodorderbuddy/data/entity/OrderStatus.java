package edu.mci.foodorderbuddy.data.entity;

public enum OrderStatus {
    IN_BEARBEITUNG("In Bearbeitung"),
    IN_ZUSTELLUNG("In Zustellung"),
    ZUGESTELLT("Zugestellt");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static OrderStatus fromDisplayName(String displayName) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.displayName.equalsIgnoreCase(displayName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unbekannter Status: " + displayName);
    }
}
