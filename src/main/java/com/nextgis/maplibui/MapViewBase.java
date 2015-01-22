/*
 * Project:  NextGIS Mobile
 * Purpose:  Mobile GIS for Android.
 * Author:   Dmitry Baryshnikov (aka Bishop), bishop.dev@gmail.com
 * *****************************************************************************
 * Copyright (c) 2012-2015. NextGIS, info@nextgis.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.nextgis.maplibui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.preference.PreferenceManager;
import android.view.View;

import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.datasource.GeoPoint;
import com.nextgis.maplib.map.LayerGroup;
import com.nextgis.maplib.map.MapDrawable;
import com.nextgis.maplib.api.MapEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.nextgis.maplib.util.GeoConstants.*;
import static com.nextgis.maplibui.util.SettingsConstants.*;


public class MapViewBase
        extends View
{

    protected MapDrawable mMap;


    public MapViewBase(
            Context context,
            MapDrawable map)
    {
        super(context);

        mMap = map;

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(KEY_PREF_KEEPSCREENON, false)) {
            setKeepScreenOn(true);
        }
    }


    public void addListener(MapEventListener listener)
    {
        if (mMap != null) {
            mMap.addListener(listener);
        }
    }


    public void removeListener(MapEventListener listener)
    {
        if (mMap != null) {
            mMap.removeListener(listener);
        }
    }


    @Override
    protected void onDraw(Canvas canvas)
    {
        if (mMap != null) {
            canvas.drawBitmap(mMap.getView(false), 0, 0, null);
        } else {
            super.onDraw(canvas);
        }
    }


    @Override
    protected void onSizeChanged(
            int w,
            int h,
            int oldw,
            int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mMap != null) {
            mMap.setViewSize(w, h);
        }
    }


    @Override
    protected void onLayout(
            boolean changed,
            int left,
            int top,
            int right,
            int bottom)
    {
        super.onLayout(changed, left, top, right, bottom);
        if (mMap != null) {
            mMap.setViewSize(right - left, bottom - top);
        }
    }

    public boolean canZoomIn()
    {
        return mMap != null && mMap.getZoomLevel() < mMap.getMaxZoom();
    }


    public boolean canZoomOut()
    {
        return mMap != null && mMap.getZoomLevel() > mMap.getMinZoom();
    }


    public final float getZoomLevel()
    {
        if (mMap == null) {
            return 0;
        }
        return mMap.getZoomLevel();
    }

    public final float getMaxZoom()
    {
        if (mMap == null) {
            return DEFAULT_MAX_ZOOM;
        }
        return mMap.getMaxZoom();
    }

    public final float getMinZoom()
    {
        if (mMap == null) {
            return 0;
        }
        return mMap.getMinZoom();
    }

    public final GeoPoint getMapCenter()
    {
        if (mMap != null) {
            return mMap.getMapCenter();
        }
        return new GeoPoint();
    }


    public void setZoomAndCenter(
            float zoom,
            GeoPoint center)
    {
        if (mMap != null) {
            mMap.setZoomAndCenter(zoom, center);
        }
    }


    public void zoomIn()
    {
        setZoomAndCenter((float) Math.ceil(getZoomLevel() + 0.5), getMapCenter());
    }


    public void zoomOut()
    {
        setZoomAndCenter((float) Math.floor(getZoomLevel() - 0.5), getMapCenter());
    }


    public void addRemoteLayer()
    {
        if (mMap != null) {
            mMap.getLayerFactory().createNewRemoteTMSLayer(getContext(), mMap);
        }
    }

    public void addNGWLayer()
    {
        if (mMap != null) {
            mMap.getLayerFactory().createNewNGWLayer(getContext(), mMap);
        }
    }

    public List<ILayer> getLayersByType(int types){
        List<ILayer> ret = new ArrayList<>();

        if (mMap != null) {
            LayerGroup.getLayersByType(mMap, types, ret);
        }

        return ret;
    }

    public List<ILayer> getVectorLayersByType(int types){
        List<ILayer> ret = new ArrayList<>();

        if (mMap != null) {
            LayerGroup.getVectorLayersByType(mMap, types, ret);
        }

        return ret;
    }

}
