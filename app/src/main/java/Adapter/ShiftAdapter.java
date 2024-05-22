package Adapter;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.nghiencuukhoahoc_appchamcong.R;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import DTO.Shift;


public class ShiftAdapter extends ArrayAdapter<Shift> {
    private Context context;
    private int resource;
    private List<Shift> shifts;

    public ShiftAdapter(Context context, int resource, List<Shift> shifts) {
        super(context, resource, shifts);
        this.context = context;
        this.resource = resource;
        this.shifts = shifts;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(resource, parent, false);
        }

        Shift shiftItem = shifts.get(position);

        if (shiftItem != null) {
            TextView txtTenLamViec = convertView.findViewById(R.id.txtTenLamViec);
            TextView txtGioBatDau = convertView.findViewById(R.id.txtGioBatDau);
            TextView txtGioKetThuc = convertView.findViewById(R.id.txtGioKetThuc);

            if (txtTenLamViec != null) {
                txtTenLamViec.setText(shiftItem.getName_Shift());
            }

            if (txtGioBatDau != null && txtGioKetThuc != null) {
                txtGioBatDau.setText(shiftItem.convertMillisToTime(shiftItem.getTime_Start()));
                txtGioKetThuc.setText(shiftItem.convertMillisToTime(shiftItem.getTime_End()));
            }
        }

        return convertView;
    }


}