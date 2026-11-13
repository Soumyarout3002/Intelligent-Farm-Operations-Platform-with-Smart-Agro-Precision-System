
import React, { useEffect, useState } from "react";
import "./CropLifecycle.css";

const STORAGE_KEY = "farmverse_crop_lifecycle";

const stages = [
  "Land Preparation",
  "Sowing",
  "Germination",
  "Vegetative Growth",
  "Flowering",
  "Harvesting"
];

export default function CropLifecycle() {
  const [crops, setCrops] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem(STORAGE_KEY)) || [];
    } catch {
      return [];
    }
  });

  const [form, setForm] = useState({
    cropName: "",
    farmName: "",
    sowingDate: "",
    expectedHarvest: "",
    currentStage: "Land Preparation",
    notes: ""
  });

  const [message, setMessage] = useState("");

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(crops));
  }, [crops]);

  const updateForm = (event) => {
    setForm({
      ...form,
      [event.target.name]: event.target.value
    });
  };

  const addCrop = (event) => {
    event.preventDefault();

    if (
      form.expectedHarvest < form.sowingDate
    ) {
      setMessage("Harvest date must be after sowing date.");
      return;
    }

    const newCrop = {
      id: Date.now(),
      ...form,
      createdAt: new Date().toISOString()
    };

    setCrops((previous) => [...previous, newCrop]);

    setForm({
      cropName: "",
      farmName: "",
      sowingDate: "",
      expectedHarvest: "",
      currentStage: "Land Preparation",
      notes: ""
    });

    setMessage("Crop added successfully!");
  };

  const updateStage = (id, newStage) => {
    setCrops((previous) =>
      previous.map((crop) =>
        crop.id === id
          ? { ...crop, currentStage: newStage }
          : crop
      )
    );

    setMessage("Crop stage updated successfully!");
  };

  const deleteCrop = (id) => {
    setCrops((previous) =>
      previous.filter((crop) => crop.id !== id)
    );

    setMessage("Crop record deleted.");
  };

  const getProgress = (stage) => {
    const index = stages.indexOf(stage);
    return Math.round(((index + 1) / stages.length) * 100);
  };

  const getDaysRemaining = (date) => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const harvest = new Date(date + "T00:00:00");
    return Math.ceil(
      (harvest - today) / (1000 * 60 * 60 * 24)
    );
  };

  const totalCrops = crops.length;
  const harvesting = crops.filter(
    (crop) => crop.currentStage === "Harvesting"
  ).length;

  return (
    <div className="crop-page">
      <h2>🌱 Crop Lifecycle Tracking</h2>

      <p>
        Monitor crop growth from land preparation to harvesting.
      </p>

      {/* Statistics */}
      <div className="crop-stats">
        <div className="crop-stat">
          <h3>🌾 Total Crops</h3>
          <h2>{totalCrops}</h2>
        </div>

        <div className="crop-stat">
          <h3>🚜 Ready for Harvest</h3>
          <h2>{harvesting}</h2>
        </div>

        <div className="crop-stat">
          <h3>🌿 Growing Crops</h3>
          <h2>{totalCrops - harvesting}</h2>
        </div>
      </div>

      {/* Add Crop Form */}
      <form className="crop-form" onSubmit={addCrop}>
        <h3>➕ Add New Crop</h3>

        <input
          name="cropName"
          placeholder="Crop Name (Rice, Wheat, Sugarcane)"
          value={form.cropName}
          onChange={updateForm}
          required
        />

        <input
          name="farmName"
          placeholder="Farm Name"
          value={form.farmName}
          onChange={updateForm}
          required
        />

        <label>Sowing Date</label>
        <input
          type="date"
          name="sowingDate"
          value={form.sowingDate}
          onChange={updateForm}
          required
        />

        <label>Expected Harvest Date</label>
        <input
          type="date"
          name="expectedHarvest"
          value={form.expectedHarvest}
          onChange={updateForm}
          required
        />

        <label>Current Crop Stage</label>
        <select
          name="currentStage"
          value={form.currentStage}
          onChange={updateForm}
        >
          {stages.map((stage) => (
            <option key={stage} value={stage}>
              {stage}
            </option>
          ))}
        </select>

        <textarea
          name="notes"
          placeholder="Notes about irrigation, fertilizer, pests, etc."
          value={form.notes}
          onChange={updateForm}
        />

        <button type="submit">Add Crop</button>
      </form>

      {message && <p className="crop-message">{message}</p>}

      {/* Crop Records */}
      <h3>📋 Crop Growth Records</h3>

      {crops.length === 0 ? (
        <p>No crop records available. Add your first crop.</p>
      ) : (
        <div className="crop-list">
          {crops.map((crop) => {
            const progress = getProgress(crop.currentStage);
            const remaining = getDaysRemaining(crop.expectedHarvest);

            return (
              <div className="crop-card" key={crop.id}>
                <div className="crop-card-header">
                  <div>
                    <h3>🌱 {crop.cropName}</h3>
                    <p>Farm: {crop.farmName}</p>
                  </div>

                  <span className="crop-progress-label">
                    {progress}% Complete
                  </span>
                </div>

                <div className="progress-track">
                  <div
                    className="progress-fill"
                    style={{ width: `${progress}%` }}
                  />
                </div>

                <p>
                  <strong>Current Stage:</strong>{" "}
                  {crop.currentStage}
                </p>

                <p>
                  <strong>Sowing:</strong> {crop.sowingDate}
                </p>

                <p>
                  <strong>Expected Harvest:</strong>{" "}
                  {crop.expectedHarvest}
                </p>

                <p>
                  <strong>Harvest Countdown:</strong>{" "}
                  {remaining < 0
                    ? `${Math.abs(remaining)} days past expected date`
                    : remaining === 0
                    ? "Expected today"
                    : `${remaining} days remaining`}
                </p>

                <p>
                  <strong>Notes:</strong>{" "}
                  {crop.notes || "No notes added"}
                </p>

                <label>Update Crop Stage</label>

                <select
                  value={crop.currentStage}
                  onChange={(event) =>
                    updateStage(crop.id, event.target.value)
                  }
                >
                  {stages.map((stage) => (
                    <option key={stage} value={stage}>
                      {stage}
                    </option>
                  ))}
                </select>

                <button
                  className="crop-delete"
                  onClick={() => deleteCrop(crop.id)}
                  type="button"
                >
                  Delete Crop
                </button>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
