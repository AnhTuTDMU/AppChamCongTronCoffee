package Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.nghiencuukhoahoc_appchamcong.R;

import java.util.List;

import DTO.Position;

public class PositionAdapter extends ArrayAdapter<Position> {
    private Context context;
    private int resource;
    private List<Position> positions;

    public PositionAdapter(Context context, int resource, List<Position> positions) {
        super(context, resource, positions);
        this.context = context;
        this.resource = resource;
        this.positions = positions;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(resource, null);
        }

        Position positionItem = positions.get(position);

        if (positionItem != null) {
            TextView txtTenChucVu = convertView.findViewById(R.id.edt_TenChucVu);
            if (txtTenChucVu != null) {
                txtTenChucVu.setText(positionItem.getName_Position());
            }
        }

        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_namepositon_user, parent, false);
        }

        TextView txt_TenCV = convertView.findViewById(R.id.txt_TenCV);
        Position positionItem = this.getItem(position);

        if (positionItem != null) {
            txt_TenCV.setText(positionItem.getName_Position());
        } else {

        }

        return convertView;
    }
    static class ViewHolder {
        TextView txt_TenNhanVien;
    }
}


