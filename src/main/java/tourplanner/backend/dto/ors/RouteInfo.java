package tourplanner.backend.dto.ors;

/**
 * Enthält das Ergebnis eines ORS-Routenaufrufs.
 * Wird vom RouteService zurückgegeben und im TourService in die Tour gespeichert.
 */
public class RouteInfo {

    /** Distanz in Kilometern (gerundet). */
    private final double distanceKm;

    /** Geschätzte Zeit in Minuten (gerundet). */
    private final int estimatedTimeMinutes;

    /**
     * Route-Koordinaten als JSON-String im Format:
     * [[lat, lng], [lat, lng], ...]
     * Wird direkt in der DB gespeichert und ans Frontend geschickt.
     */
    private final String routeCoordinatesJson;

    public RouteInfo(double distanceKm, int estimatedTimeMinutes, String routeCoordinatesJson) {
        this.distanceKm = distanceKm;
        this.estimatedTimeMinutes = estimatedTimeMinutes;
        this.routeCoordinatesJson = routeCoordinatesJson;
    }

    public double getDistanceKm() { return distanceKm; }
    public int getEstimatedTimeMinutes() { return estimatedTimeMinutes; }
    public String getRouteCoordinatesJson() { return routeCoordinatesJson; }
}
