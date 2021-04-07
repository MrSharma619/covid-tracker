package com.example.coronavirustracker.services;

import com.example.coronavirustracker.models.LocationStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class CoronaVirusDataService {
    private static String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";

    private List<LocationStats> allStats = new ArrayList<>();

    public List<LocationStats> getAllStats() {
        return allStats;
    }

    @PostConstruct
    @Scheduled(cron = "* * 1 * * *")
    public void fetchVirusData(){
        List<LocationStats> newStats = new ArrayList<>();        //to solve concurrency issues

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(VIRUS_DATA_URL))
                .build();

        HttpResponse<String> httpResponse = null;
        try {
            httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            System.out.println(e);
        } catch (InterruptedException e) {
            System.out.println(e);
        }

        //System.out.println(httpResponse.body());

        StringReader stringReader = new StringReader(httpResponse.body());
        Iterable<CSVRecord> records = null;
        try {
            records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(stringReader);
        } catch (IOException e) {
            System.out.println(e);
        }
        for (CSVRecord record : records) {
            //String state = record.get("Province/State");
            //System.out.println(state);
            LocationStats locationStats = new LocationStats();

            locationStats.setState(record.get("Province/State"));
            locationStats.setCountry(record.get("Country/Region"));

            int latestData = Integer.parseInt(record.get(record.size() - 1));
            int prevDayData = Integer.parseInt(record.get(record.size() - 2));

            locationStats.setLatestTotalCases(latestData);    //depending on date
            locationStats.setDiffFromPrevDay(latestData - prevDayData);

            //System.out.println(locationStats);
            newStats.add(locationStats);
        }
        this.allStats = newStats;

    }
}
