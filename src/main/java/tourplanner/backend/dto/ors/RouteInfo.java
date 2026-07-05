package tourplanner.backend.dto.ors;

/**
 * Contains the result of an ORS route call.
 * Returned by RouteService and saved to the tour in TourService.
 */
public class RouteInfo {

    private final double distanceKm;
    private final int estimatedTimeMinutes;

    /**
     * Route-Coordinates as JSON-String in Format:
     * [[lat, lng], [lat, lng], ...]
     * It is stored directly in the database and sent to the front end.
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
