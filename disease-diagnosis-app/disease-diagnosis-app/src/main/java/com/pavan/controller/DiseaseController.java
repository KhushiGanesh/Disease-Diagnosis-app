package com.pavan.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpSession;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.pavan.model.PredictionHistory;
import com.pavan.repository.PredictionHistoryRepository;

@Controller
public class DiseaseController {

    private final PredictionHistoryRepository predictionRepo;

    public DiseaseController(PredictionHistoryRepository predictionRepo) {
        this.predictionRepo = predictionRepo;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @PostMapping("/predict")
    public String predictDisease(@RequestParam("symptom1") String symptom1,
                                 @RequestParam("symptom2") String symptom2,
                                 @RequestParam("symptom3") String symptom3,
                                 Model model) {

        RestTemplate restTemplate = new RestTemplate();
        String flaskUrl = "http://127.0.0.1:5000/predict";

        JSONObject json = new JSONObject();
        json.put("symptoms", Arrays.asList(symptom1, symptom2, symptom3));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(json.toString(), headers);

        ResponseEntity<String> response = restTemplate.postForEntity(flaskUrl, request, String.class);
        JSONObject responseObject = new JSONObject(response.getBody());

        String prediction = responseObject.getString("prediction");
        String description = responseObject.getString("description");

        JSONArray precautionsArray = responseObject.getJSONArray("precautions");
        List<String> precautions = new ArrayList<>();
        for (int i = 0; i < precautionsArray.length(); i++) {
            precautions.add(precautionsArray.getString(i));
        }

        model.addAttribute("prediction", prediction);
        model.addAttribute("description", description);
        model.addAttribute("precautions", precautions);

        // Save to DB
        
        String precautionsStr = String.join(", ", precautions);
        PredictionHistory historyRecord = new PredictionHistory(prediction, description, precautionsStr);
        predictionRepo.save(historyRecord);

        return "index";
    }

    @GetMapping("/text-input")
    public String textInputPage() {
        return "text-input";
    }

    @PostMapping("/predict-from-text")
    public String predictFromText(@RequestParam("userText") String userText,
                                  Model model,
                                  HttpSession session) {

        RestTemplate restTemplate = new RestTemplate();
        String flaskUrl = "http://127.0.0.1:5000/predict";

        List<String> symptoms = Arrays.asList(userText.toLowerCase().split("\\s+and\\s+|,|\\s+"));

        JSONObject json = new JSONObject();
        json.put("symptoms", symptoms);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(json.toString(), headers);

        ResponseEntity<String> response = restTemplate.postForEntity(flaskUrl, request, String.class);
        JSONObject responseObject = new JSONObject(response.getBody());

        String prediction = responseObject.getString("prediction");
        String description = responseObject.getString("description");

        JSONArray precautionsArray = responseObject.getJSONArray("precautions");
        List<String> precautions = new ArrayList<>();
        for (int i = 0; i < precautionsArray.length(); i++) {
            precautions.add(precautionsArray.getString(i));
        }

        model.addAttribute("prediction", prediction);
        model.addAttribute("description", description);
        model.addAttribute("precautions", precautions);

        // Save to DB
        String precautionsStr = String.join(", ", precautions);
        PredictionHistory historyRecord = new PredictionHistory(prediction, description, precautionsStr);
        predictionRepo.save(historyRecord);

        // Add to session history
        List<String> history = (List<String>) session.getAttribute("history");
        if (history == null) history = new ArrayList<>();
        history.add(prediction);
        session.setAttribute("history", history);
        model.addAttribute("history", history);

        return "text-input";
    }
}
