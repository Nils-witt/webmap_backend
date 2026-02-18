package dev.nilswitt.webmap.entities;

public interface PositionInterface  {
    double getLatitude();
    double getLongitude();
    double getAltitude();
    double getAccuracy();
    void setLatitude(double latitude);
    void setLongitude(double longitude);
    void setAltitude(double altitude);
    void setAccuracy(double accuracy);
}
