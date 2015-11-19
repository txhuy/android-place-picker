package com.github.stephenbaidu.placepicker;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by stephen on 3/3/15.
 */
public class PlaceHistoryManager {

    private static PlaceHistoryManager instance = null;

    private final int RECENT_HISTORY_SIZE = 4;
    private final int HISTORY_SIZE = 4;

    private Context context;

    public static PlaceHistoryManager getInstance(Context context) {
        if(instance == null) {
            instance = new PlaceHistoryManager();
        }

        instance.context = context;

        return instance;
    }

    public List<PlaceInfo> getHistoryRecords() {
        List<PlaceInfo> history = new ArrayList<PlaceInfo>();

        for (int i = 0; i < HISTORY_SIZE; ++i) {
            PlaceInfo placeInfo = getHistoryRecord(i + 1);

            if(placeInfo == null) {
                break;
            } else {
                history.add(placeInfo);
            }
        }

        Collections.reverse(history);

        return history;
    }

    private PlaceInfo getHistoryRecord(int index) {
        if(index <= 0 || index > HISTORY_SIZE) {
            return null;
        }

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.context);
        PlaceInfo placeInfo = null;

        String placeId = sp.getString("pref_place_picker_history_place_id_" + index, "");
        String name = sp.getString("pref_place_picker_history_name_" + index, "");
        String description = sp.getString("pref_place_picker_history_description_" + index, "");

        if(placeId.length() > 0) {
            placeInfo = new PlaceInfo(placeId, name, description);
        }

        return placeInfo;
    }

    private void putHistoryRecord(PlaceInfo placeInfo, int index) {
        if(index <= 0 || index > HISTORY_SIZE) {
            return;
        }

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.context);

        if(placeInfo != null) {
            sp.edit().putString("pref_place_picker_history_place_id_" + Integer.toString(index), placeInfo.placeId).apply();
            sp.edit().putString("pref_place_picker_history_name_" + Integer.toString(index), placeInfo.name).apply();
            sp.edit().putString("pref_place_picker_history_description_" + Integer.toString(index), placeInfo.description).apply();
        }
    }

    private void addToHistory(PlaceInfo placeInfo) {
        int newIndex = getHistoryRecords().size() + 1;

        // History is full
        if(newIndex > HISTORY_SIZE) {
            // Remove oldest record
            for (int i = 0; i < HISTORY_SIZE; ++i) {
                putHistoryRecord(getHistoryRecord(i + 2), i + 1);
            }
            newIndex = HISTORY_SIZE;
        }

        putHistoryRecord(placeInfo, newIndex);
    }

    public void updateHistory(PlaceInfo placeInfo) {
        int index = findHistoryRecord(placeInfo.placeId);

        // History record does not exist
        if (index == 0) {
            addToHistory(placeInfo);
            return;
        }

        // If last record, leave at current index
        if (getHistoryRecord(index + 1) == null) {
            return;
        }

        removeFromHistory(index);
        addToHistory(placeInfo);
    }

    private int findHistoryRecord(String placeId) {
        for (int i = 0; i < HISTORY_SIZE; ++i) {
            PlaceInfo placeInfo = getHistoryRecord(i + 1);

            if (placeInfo == null) return 0;

            if (placeInfo.placeId == placeId) {
                return i + 1;
            }
        }

        return 0;
    }

    private void removeFromHistory(int index) {
        // Will return null for invalid index
        PlaceInfo placeInfo = getHistoryRecord(index);
        PlaceInfo nextRecord;

        // Record does not exist
        if (placeInfo == null) return;

        nextRecord = getHistoryRecord(index + 1);

        // It is the last record
        if (nextRecord == null) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.context);

            // Clear values
            sp.edit().putString("pref_place_picker_history_place_id_" + Integer.toString(index), "").apply();
            sp.edit().putString("pref_place_picker_history_name_" + Integer.toString(index), "").apply();
            sp.edit().putString("pref_place_picker_history_description_" + Integer.toString(index), "").apply();
        } else {
            putHistoryRecord(nextRecord, index);
            removeFromHistory(index + 1);
        }
    }

    public List<PlaceInfo> getRecentHistory() {
        List<PlaceInfo> historyRecords = getHistoryRecords();
        int subListEndIndex = RECENT_HISTORY_SIZE;

        if(historyRecords.size() < RECENT_HISTORY_SIZE) {
            subListEndIndex = historyRecords.size();
        }

        historyRecords = historyRecords.subList(0, subListEndIndex);
//        historyRecords.add(new PlaceInfo("MORE FROM RECENT HISTORY"));

        return historyRecords;
    }
}
