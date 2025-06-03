from flask import Flask, request, jsonify
from flask_cors import CORS
import pickle
import numpy as np
import pandas as pd

# Initialize Flask app
app = Flask(__name__)
CORS(app)

# Load model, label encoder, and symptom list
with open("model/disease_predictor.pkl", "rb") as f:
    model, label_encoder, all_symptoms = pickle.load(f)

# Load disease description and precautions
desc_df = pd.read_csv("symptom_Description.csv")
prec_df = pd.read_csv("symptom_precaution.csv")

@app.route("/", methods=["GET"])
def home():
    return jsonify({
        "message": "Welcome to Disease Prediction API",
        "available_symptoms": all_symptoms
    })

@app.route("/predict", methods=["POST"])
def predict():
    data = request.get_json()

    if not data or "symptoms" not in data:
        return jsonify({"error": "Please provide 'symptoms' field"}), 400

    input_symptoms = [s.lower().strip() for s in data["symptoms"]]
    input_vector = [1 if symptom.lower() in input_symptoms else 0 for symptom in all_symptoms]
    input_np = np.array(input_vector).reshape(1, -1)

    prediction_index = model.predict(input_np)[0]
    predicted_disease = label_encoder.inverse_transform([prediction_index])[0]

    # Fetch description
    description_row = desc_df[desc_df["Disease"].str.lower() == predicted_disease.lower()]
    description = description_row["Description"].values[0] if not description_row.empty else "No description available."

    # Fetch precautions
    precautions_row = prec_df[prec_df["Disease"].str.lower() == predicted_disease.lower()]
    if not precautions_row.empty:
        precautions = [
            precautions_row["Precaution_1"].values[0],
            precautions_row["Precaution_2"].values[0],
            precautions_row["Precaution_3"].values[0],
            precautions_row["Precaution_4"].values[0],
        ]
    else:
        precautions = ["No precautions available."]

    return jsonify({
        "prediction": predicted_disease,
        "description": description,
        "precautions": precautions
    })


if __name__ == "__main__":
    app.run(debug=True)
