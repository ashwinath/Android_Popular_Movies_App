/*
 * Copyright 2016 Ashwin Nath Chatterji
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.ashwin.popularmovies;

import android.util.Log;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

/**
 * Created by ashwin on 7/3/2016.
 */
public class Utility {
    public static String formatDate(String date) {
        // input date will be like 2015-03-09
        List<String> dateList = Arrays.asList(date.split("-"));
        int day = Integer.parseInt(dateList.get(2));
        int month = Integer.parseInt(dateList.get(1));
        int year = Integer.parseInt(dateList.get(0));
        String monthString;
        switch (month) {
            case 1:
                monthString = "January";
                break;
            case 2:
                monthString = "February";
                break;
            case 3:
                monthString = "March";
                break;
            case 4:
                monthString = "April";
                break;
            case 5:
                monthString = "May";
                break;
            case 6:
                monthString = "June";
                break;
            case 7:
                monthString = "July";
                break;
            case 8:
                monthString = "August";
                break;
            case 9:
                monthString = "September";
                break;
            case 10:
                monthString = "October";
                break;
            case 11:
                monthString = "November";
                break;
            case 12:
                monthString = "December";
                break;
            default:
                monthString = "Unknown";
        }
        return day + " " + monthString + " " + year;
    }

    public static String formatRatings(double rating) {
        return rating + "/10";
    }

    public static String formatGenres(String genreIds) {
        List<String> genreIdList = Arrays.asList(genreIds.split(","));
        if (genreIdList.get(0).equals(""))
            return "";
        String[] genreArray = new String[genreIdList.size()];
        for (int i = 0; i < genreIdList.size(); ++i) {
            int id = Integer.parseInt(genreIdList.get(i));
            switch (id) {
                case 28:
                    genreArray[i] = "Action";
                    break;
                case 12:
                    genreArray[i] = "Adventure";
                    break;
                case 16:
                    genreArray[i] = "Animation";
                    break;
                case 35:
                    genreArray[i] = "Comedy";
                    break;
                case 80:
                    genreArray[i] = "Crime";
                    break;
                case 99:
                    genreArray[i] = "Documentary";
                    break;
                case 18:
                    genreArray[i] = "Drama";
                    break;
                case 10751:
                    genreArray[i] = "Family";
                    break;
                case 14:
                    genreArray[i] = "Fantasy";
                    break;
                case 10769:
                    genreArray[i] = "Foreign";
                    break;
                case 36:
                    genreArray[i] = "History";
                    break;
                case 27:
                    genreArray[i] = "Horror";
                    break;
                case 10402:
                    genreArray[i] = "Music";
                    break;
                case 9648:
                    genreArray[i] = "Mystery";
                    break;
                case 10749:
                    genreArray[i] = "Romance";
                    break;
                case 878:
                    genreArray[i] = "Science Fiction";
                    break;
                case 10770:
                    genreArray[i] = "TV Movie";
                    break;
                case 53:
                    genreArray[i] = "Thriller";
                    break;
                case 10752:
                    genreArray[i] = "War";
                    break;
                case 37:
                    genreArray[i] = "Western";
                    break;
                default:
                    genreArray[i] = "unknown";
            }
        }
        String returnString = "";
        for (int i = 0; i < genreArray.length; ++i) {
            if (i == genreArray.length -1)
                returnString += genreArray[i];
            else
                returnString += genreArray[i] + ", ";
        }
        return returnString;
    }
}
