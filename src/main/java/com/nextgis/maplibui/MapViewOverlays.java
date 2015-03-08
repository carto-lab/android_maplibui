/*
 * Project:  NextGIS Mobile
 * Purpose:  Mobile GIS for Android.
 * Author:   Stanislav Petriakov, becomeglory@gmail.com
 * *****************************************************************************
 * Copyright (c) 2015 NextGIS, info@nextgis.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
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
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import com.nextgis.maplib.datasource.GeoPoint;
import com.nextgis.maplib.map.MapDrawable;
import com.nextgis.maplibui.api.Overlay;

import java.util.ArrayList;
import java.util.List;

import static com.nextgis.maplibui.util.ConstantsUI.*;


public class MapViewOverlays
        extends MapView
{
    protected List<Overlay> mOverlays;
    protected boolean mLockMap;
    protected boolean mSkipNextDraw;
    protected long mDelay;

    public MapViewOverlays(
            Context context,
            MapDrawable map)
    {
        super(context, map);
        mOverlays = new ArrayList<>();
        mLockMap = false;
        mSkipNextDraw = false;
        mDelay = 0;
    }


    public boolean isLockMap()
    {
        return mLockMap;
    }


    public void setLockMap(boolean lockMap)
    {
        mLockMap = lockMap;
    }


    @Override
    protected void onDraw(Canvas canvas)
    {
        if(isLockMap())
            canvas.drawBitmap(mMap.getView(false), 0, 0, null);
        else
            super.onDraw(canvas);

        if (mMap != null) {
            switch (mDrawingState) {
                case DRAW_SATE_drawing:
                case DRAW_SATE_drawing_noclearbk:
                    for (Overlay overlay : mOverlays) {
                        overlay.draw(canvas, mMap);
                    }
                    break;
                case DRAW_SATE_panning:
                case DRAW_SATE_panning_fling:
                    for (Overlay overlay : mOverlays) {
                        overlay.drawOnPanning(canvas, mCurrentMouseOffset);
                    }
                    break;
                case DRAW_SATE_zooming:
                    for (Overlay overlay : mOverlays) {
                        overlay.drawOnZooming(canvas, mCurrentFocusLocation, (float) mScaleFactor);
                    }
                    break;
            }
        }
    }


    public MapDrawable getMap()
    {
        return mMap;
    }


    public void addOverlay(Overlay overlay)
    {
        mOverlays.add(overlay);
    }


    public void removeOverlay(Overlay overlay)
    {
        mOverlays.remove(overlay);
    }


    public List<Overlay> getOverlays()
    {
        return mOverlays;
    }


    @Override
    protected Parcelable onSaveInstanceState()
    {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);

        for(Overlay overlay : mOverlays) {
            savedState.add(overlay.onSaveState());
        }

        return savedState;
    }


    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if (state instanceof SavedState) {
            SavedState savedState = (SavedState) state;

            int counter = 0;
            for(Overlay overlay : mOverlays) {
                overlay.onRestoreState(savedState.get(counter++));
            }

            super.onRestoreInstanceState(savedState.getSuperState());
        } else {
            super.onRestoreInstanceState(state);
        }
    }


    @Override
    public void setZoomAndCenter(
            float zoom,
            GeoPoint center)
    {
        if(isLockMap()){
            mDrawingState = DRAW_SATE_drawing_noclearbk;
            return;
        }
        super.setZoomAndCenter(zoom, center);
    }


    public static class SavedState
            extends BaseSavedState
    {
        protected List<Bundle> mBundles;


        public SavedState(Parcelable parcel)
        {
            super(parcel);
            mBundles = new ArrayList<>();
        }


        private SavedState(Parcel in)
        {
            super(in);

            mBundles = new ArrayList<>();
            int size = in.readInt();
            for(int i = 0; i < size; i++){
                Bundle bundle = in.readBundle();
                mBundles.add(bundle);
            }
        }

        public void add(Bundle bundle){
            mBundles.add(bundle);
        }

        public Bundle get(int index){
            return mBundles.get(index);
        }

        @Override
        public void writeToParcel(
                @NonNull
                Parcel out,
                int flags)
        {
            super.writeToParcel(out, flags);

            out.writeInt(mBundles.size());
            for(Bundle bundle : mBundles) {
                out.writeBundle(bundle);
            }
        }


        public static final Creator<SavedState> CREATOR = new Creator<SavedState>()
        {

            @Override
            public SavedState createFromParcel(Parcel in)
            {
                return new SavedState(in);
            }


            @Override
            public SavedState[] newArray(int size)
            {
                return new SavedState[size];
            }
        };
    }


    public void setSkipNextDraw(boolean skipNextDraw)
    {
        mSkipNextDraw = skipNextDraw;
    }


    @Override
    public void onLayerChanged(int id)
    {
        if(mSkipNextDraw){
            mSkipNextDraw = false;
            return;
        }
        //delay execution
        if(mDelay > 0){
            final int thisId = id;
            Handler handler = new Handler(Looper.getMainLooper());
            final Runnable r = new Runnable() {
                public void run() {
                    //do your stuff here after DELAY sec
                    onLayerChanged(thisId);
                }
            };
            handler.postDelayed(r, mDelay);
            mDelay = 0;
        }
        else{
            super.onLayerChanged(id);
        }
    }


    public void setDelay(long delay)
    {
        mDelay = delay;
    }
}
