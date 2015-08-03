/*
 * Project:  NextGIS Mobile
 * Purpose:  Mobile GIS for Android.
 * Author:   Stanislav Petriakov, becomeglory@gmail.com
 * *****************************************************************************
 * Copyright (c) 2015 NextGIS, info@nextgis.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.nextgis.maplibui.formcontrol;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.nextgis.maplib.datasource.Field;
import com.nextgis.maplibui.api.IFormControl;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static com.nextgis.maplibui.util.ConstantsUI.JSON_ATTRIBUTES_KEY;
import static com.nextgis.maplibui.util.ConstantsUI.JSON_CHECKBOX_INIT_KEY;
import static com.nextgis.maplibui.util.ConstantsUI.JSON_FIELD_NAME_KEY;
import static com.nextgis.maplibui.util.ConstantsUI.JSON_SHOW_LAST_KEY;
import static com.nextgis.maplibui.util.ConstantsUI.JSON_TEXT_KEY;

public class Checkbox extends CheckBox implements IFormControl {
    protected String mFieldName;
    protected boolean mIsShowLast;

    public Checkbox(Context context) {
        super(context);
    }

    public Checkbox(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void init(JSONObject element,
                     List<Field> fields,
                     Cursor featureCursor,
                     SharedPreferences preferences) throws JSONException {

        JSONObject attributes = element.getJSONObject(JSON_ATTRIBUTES_KEY);
        mFieldName = attributes.getString(JSON_FIELD_NAME_KEY);
        boolean isEnabled = false;

        for (Field field : fields) {
            String fieldName = field.getName();

            if (fieldName.equals(mFieldName)) {
                isEnabled = true;
                break;
            }
        }

        setEnabled(isEnabled);

        mIsShowLast = false;
        if (attributes.has(JSON_SHOW_LAST_KEY) && !attributes.isNull(JSON_SHOW_LAST_KEY)) {
            mIsShowLast = attributes.getBoolean(JSON_SHOW_LAST_KEY);
        }

        Boolean value = null;
        if (null != featureCursor) {
            int column = featureCursor.getColumnIndex(mFieldName);

            if (column >= 0)
                value = featureCursor.getInt(column) != 0;
        } else {
            value = attributes.getBoolean(JSON_CHECKBOX_INIT_KEY);

            if (mIsShowLast)
                value = preferences.getBoolean(mFieldName, value);
        }

        if (value == null)
            value = false;

        setChecked(value);
        setText(attributes.getString(JSON_TEXT_KEY));
    }

    public String getFieldName() {
        return mFieldName;
    }

    @Override
    public void addToLayout(ViewGroup layout) {
        layout.addView(this);
    }

    @Override
    public Object getValue() {
        return isChecked() ? 1 : 0;
    }

    @Override
    public void saveLastValue(SharedPreferences preferences) {
        preferences.edit().putBoolean(mFieldName, isChecked()).commit();
    }

    @Override
    public boolean isShowLast() {
        return mIsShowLast;
    }

}