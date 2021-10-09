//
// Treasure Data Bulk-Import Tool in Java
//
// Copyright (C) 2012 - 2013 Muga Nishizawa
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//        http://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//
package com.treasure_data.td_import.model;

import java.math.BigInteger;

public class ColumnSampling {
    protected int numRows;
    protected int[] scores = new int[] { 0, 0, 0, 0, 0, 0 };

    public ColumnSampling(int numRows) {
        this.numRows = numRows;
    }

    public void parse(String value) {
        if (value == null) {
            // any score are not changed
            return;
        }

        // value looks like String object?
        scores[ColumnType.STRING.getOrderIndex()] += 1;

        // value looks like Double object?
        try {
            Double.parseDouble((String) value);
            scores[ColumnType.DOUBLE.getOrderIndex()] += 1;
        } catch (NumberFormatException e) {
            // ignore
        }

        // value looks like Float object?
        try {
            Float.parseFloat((String) value);
            scores[ColumnType.FLOAT.getOrderIndex()] += 1;
        } catch (NumberFormatException e) {
            // ignore
        }

        // value looks like BigInteger object?
        try {
            new BigInteger((String) value);
            scores[ColumnType.BIGINT.getOrderIndex()] += 1;
        } catch (NumberFormatException e) {
            // ignore
        }

        // value looks like Long object?
        try {
            Long.parseLong((String) value);
            scores[ColumnType.LONG.getOrderIndex()] += 1;
        } catch (NumberFormatException e) {
            // ignore
        }

        // value looks like Integer object?
        try {
            Integer.parseInt((String) value);
            scores[ColumnType.INT.getOrderIndex()] += 1;
        } catch (NumberFormatException e) {
            // ignore
        }
    }

    public ColumnType getRank() {
        int max = -numRows;
        int maxIndex = 0;
        for (int i = 0; i < scores.length; i++) {
            if (max <= scores[i]) {
                max = scores[i];
                maxIndex = i;
            }
        }

        ColumnType ret = ColumnType.Conv.fromInt(maxIndex);
        if (ret.equals(ColumnType.BIGINT)) {
            return ColumnType.STRING;
        }
        return ret;
    }
}