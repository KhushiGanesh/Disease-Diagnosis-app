import pandas as pd
import pickle
from sklearn.naive_bayes import MultinomialNB
from sklearn.preprocessing import LabelEncoder

# Load dataset
df = pd.read_csv("dataset.csv")

# Clean column names
df.columns = df.columns.str.strip()

# Replace NaN with empty string (valid)
df = df.fillna("")

# Get symptom columns
symptom_columns = [col for col in df.columns if col.startswith("Symptom")]

# Collect all unique symptoms
all_symptoms = set()
for col in symptom_columns:
    all_symptoms.update(df[col].unique())

# Remove empty and strip spaces
all_symptoms = sorted([s.strip() for s in all_symptoms if s.strip() != ""])

# Create one-hot encoded symptom matrix
encoded_data = []
for _, row in df.iterrows():
    symptoms_present = set([s.strip() for s in row[symptom_columns] if s.strip() != ""])
    row_encoded = [1 if symptom in symptoms_present else 0 for symptom in all_symptoms]
    encoded_data.append(row_encoded)

X = pd.DataFrame(encoded_data, columns=all_symptoms)

# Encode disease labels
le = LabelEncoder()
y = le.fit_transform(df['Disease'])

# Train model
model = MultinomialNB()
model.fit(X, y)

# Save model and extras
with open("model/disease_predictor.pkl", "wb") as f:
    pickle.dump((model, le, all_symptoms), f)

print("Model training completed and saved.")
