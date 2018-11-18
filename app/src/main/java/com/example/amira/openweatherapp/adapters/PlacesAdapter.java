package com.example.amira.openweatherapp.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.amira.openweatherapp.R;
import com.example.amira.openweatherapp.data.DbContract;

public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.PlaceViewHolder> {

    private static final String LOG_TAG = PlacesAdapter.class.getSimpleName();
    private Context mContext;

    private Cursor mCursor;
    private int numberOfItems;

    private OnItemClickHandler mHandler;

    public PlacesAdapter(OnItemClickHandler handler){
        this.mHandler = handler;
    }

    public void setmCursor(Cursor mCursor) {
        this.mCursor = mCursor;
        if(mCursor != null) this.numberOfItems = mCursor.getCount();
        else this.numberOfItems  = 0;
    }

    @Override
    public PlaceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.places_rv_item , parent , false);
        PlaceViewHolder vh = new PlaceViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(PlaceViewHolder holder, int position) {
        if(mCursor != null && mCursor.moveToPosition(position)){
            holder.mPlaceTemperature.setText(mCursor.getString(mCursor.getColumnIndex(DbContract.OpenWeatherDbEntry.TEMP_COL)));
            holder.mPlaceName.setText(mCursor.getString(mCursor.getColumnIndex(DbContract.OpenWeatherDbEntry.NAME_COL)));
        }else{
            Log.d(LOG_TAG , "Null Cursor");
        }
    }

    @Override
    public int getItemCount() {
        return this.numberOfItems;
    }

    class PlaceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView mPlaceName, mPlaceTemperature;

        public PlaceViewHolder(View itemView) {
            super(itemView);
            mPlaceName = itemView.findViewById(R.id.tv_place_name);
            mPlaceTemperature = itemView.findViewById(R.id.tv_temperature);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            mHandler.onClick(position);
        }
    }

    public interface OnItemClickHandler{
        void onClick(int position);
    }
}
