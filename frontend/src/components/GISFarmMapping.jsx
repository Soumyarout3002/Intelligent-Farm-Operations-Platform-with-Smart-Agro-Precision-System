
import React, { useState, useEffect } from "react";
import {
  MapContainer,
  TileLayer,
  Marker,
  Popup,
  useMapEvents
} from "react-leaflet";

import "leaflet/dist/leaflet.css";
import L from "leaflet";

// Fix Leaflet marker icons in Vite
delete L.Icon.Default.prototype._getIconUrl;

L.Icon.Default.mergeOptions({
  iconRetinaUrl:
    "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png",
  iconUrl:
    "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png",
  shadowUrl:
    "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png"
});

const STORAGE_KEY = "farmverse_gis_plots";

// Handle map clicks
function MapClickHandler({ onSelectLocation }) {
  useMapEvents({
    click(event) {
      onSelectLocation(
        event.latlng.lat,
        event.latlng.lng
      );
    }
  });

  return null;
}

export default function GISFarmMapping() {

  const [plots, setPlots] = useState(() => {
    try {
      const savedPlots = localStorage.getItem(STORAGE_KEY);
      return savedPlots ? JSON.parse(savedPlots) : [];
    } catch {
      return [];
    }
  });

  const [plotName, setPlotName] = useState("");
  const [cropType, setCropType] = useState("");
  const [area, setArea] = useState("");
  const [latitude, setLatitude] = useState("");
  const [longitude, setLongitude] = useState("");
  const [currentLocation, setCurrentLocation] = useState(null);
  const [message, setMessage] = useState("");

  // Default map center only, not an actual farm location
  const defaultCenter = [18.1517, 74.5777];

  // Save plots in browser storage
  useEffect(() => {
    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify(plots)
    );
  }, [plots]);

  // Get device GPS location
  const getGPSLocation = () => {

    if (!navigator.geolocation) {
      setMessage("GPS is not supported by this browser.");
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {

        const lat = position.coords.latitude;
        const lng = position.coords.longitude;

        setCurrentLocation([lat, lng]);
        setLatitude(lat.toFixed(6));
        setLongitude(lng.toFixed(6));

        setMessage("Current GPS location detected successfully!");
      },

      (error) => {
        setMessage(
          error.code === 1
            ? "Please allow location permission in your browser."
            : "Unable to detect location. Enter coordinates manually."
        );
      },

      {
        enableHighAccuracy: true,
        timeout: 15000
      }
    );
  };

  // Select location by clicking the map
  const selectMapLocation = (lat, lng) => {

    setLatitude(lat.toFixed(6));
    setLongitude(lng.toFixed(6));

    setMessage(
      "Map location selected. Enter plot details and add the plot."
    );
  };

  // Add new farm plot
  const addPlot = (event) => {

    event.preventDefault();

    const lat = Number(latitude);
    const lng = Number(longitude);
    const plotArea = Number(area);

    if (
      !plotName.trim() ||
      !cropType.trim() ||
      !Number.isFinite(plotArea) ||
      plotArea <= 0 ||
      !Number.isFinite(lat) ||
      lat < -90 ||
      lat > 90 ||
      !Number.isFinite(lng) ||
      lng < -180 ||
      lng > 180
    ) {
      setMessage("Please enter valid plot details and coordinates.");
      return;
    }

    const newPlot = {
      id: Date.now(),
      plotName: plotName.trim(),
      cropType: cropType.trim(),
      area: plotArea,
      latitude: lat,
      longitude: lng
    };

    setPlots((previous) => [...previous, newPlot]);

    setPlotName("");
    setCropType("");
    setArea("");
    setLatitude("");
    setLongitude("");

    setMessage("Farm plot added successfully!");
  };

  // Delete farm plot
  const deletePlot = (id) => {

    setPlots((previous) =>
      previous.filter((plot) => plot.id !== id)
    );

    setMessage("Farm plot deleted successfully.");
  };

  // Calculate total area
  const totalArea = plots.reduce(
    (total, plot) => total + Number(plot.area),
    0
  );

  // Map center
  const mapCenter =
    currentLocation ||
    (plots.length > 0
      ? [plots[0].latitude, plots[0].longitude]
      : defaultCenter);

  return (
    <div className="gis-container">

      <h2>🗺️ GIS Farm Mapping</h2>

      <p>
        Manage farm plots and view their geographical locations.
      </p>

      {/* Statistics */}
      <div className="gis-stats">

        <div className="gis-stat-card">
          <h3>📍 Total Plots</h3>
          <h2>{plots.length}</h2>
        </div>

        <div className="gis-stat-card">
          <h3>🌱 Cultivated Area</h3>
          <h2>{totalArea.toFixed(2)} acres</h2>
        </div>

      </div>

      {/* Add Plot Form */}
      <form className="gis-form" onSubmit={addPlot}>

        <h3>Add New Farm Plot</h3>

        <input
          type="text"
          placeholder="Enter Plot Name"
          value={plotName}
          onChange={(e) => setPlotName(e.target.value)}
          required
        />

        <input
          type="text"
          placeholder="Enter Crop Type"
          value={cropType}
          onChange={(e) => setCropType(e.target.value)}
          required
        />

        <input
          type="number"
          min="0.01"
          step="0.01"
          placeholder="Area in Acres"
          value={area}
          onChange={(e) => setArea(e.target.value)}
          required
        />

        <input
          type="number"
          step="any"
          placeholder="Latitude"
          value={latitude}
          onChange={(e) => setLatitude(e.target.value)}
          required
        />

        <input
          type="number"
          step="any"
          placeholder="Longitude"
          value={longitude}
          onChange={(e) => setLongitude(e.target.value)}
          required
        />

        <button type="button" onClick={getGPSLocation}>
          📍 Get Current GPS Location
        </button>

        <button type="submit">
          + Add Farm Plot
        </button>

      </form>

      {message && (
        <p className="gis-message">{message}</p>
      )}

      {/* Interactive Map */}
      <h3>🌍 Farm Location Map</h3>

      <p>
        Click anywhere on the map to select farm coordinates.
      </p>

      <div
        className="gis-map"
        style={{
          height: "450px",
          width: "100%",
          margin: "20px 0",
          borderRadius: "12px",
          overflow: "hidden",
          border: "1px solid #ddd",
          position: "relative",
          zIndex: 1
        }}
      >

        <MapContainer
          key={mapCenter.join(",")}
          center={mapCenter}
          zoom={13}
          scrollWheelZoom={true}
          style={{
            height: "100%",
            width: "100%"
          }}
        >

          {/* OpenStreetMap tiles */}
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
            url="https://tile.openstreetmap.org/{z}/{x}/{y}.png"
          />

          {/* Map click selection */}
          <MapClickHandler
            onSelectLocation={selectMapLocation}
          />

          {/* Current GPS marker */}
          {currentLocation && (
            <Marker position={currentLocation}>
              <Popup>
                Your current device location
              </Popup>
            </Marker>
          )}

          {/* Farm plot markers */}
          {plots.map((plot) => (

            <Marker
              key={plot.id}
              position={[
                plot.latitude,
                plot.longitude
              ]}
            >

              <Popup>
                <strong>{plot.plotName}</strong>
                <br />
                Crop: {plot.cropType}
                <br />
                Area: {plot.area} acres
                <br />
                Latitude: {plot.latitude}
                <br />
                Longitude: {plot.longitude}
              </Popup>

            </Marker>

          ))}

        </MapContainer>

      </div>

      {/* Farm Plot Details */}
      <h3>🌾 Farm Plot Details</h3>

      {plots.length === 0 ? (

        <p>No plots added yet. Add your first farm plot.</p>

      ) : (

        <div className="gis-plot-list">

          {plots.map((plot) => (

            <div
              className="gis-plot-card"
              key={plot.id}
            >

              <h4>🌱 {plot.plotName}</h4>

              <p>Crop: {plot.cropType}</p>

              <p>Area: {plot.area} acres</p>

              <p>Latitude: {plot.latitude}</p>

              <p>Longitude: {plot.longitude}</p>

              <button
                type="button"
                onClick={() => deletePlot(plot.id)}
              >
                Delete Plot
              </button>

            </div>

          ))}

        </div>

      )}

    </div>
  );
}
