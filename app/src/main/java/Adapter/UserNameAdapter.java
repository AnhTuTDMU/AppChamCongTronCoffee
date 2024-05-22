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

import DTO.Users;

public class UserNameAdapter extends ArrayAdapter<Users> {
    private Context context;
    private int resource;
    private List<Users> usersList;

    public UserNameAdapter(Context context, int resource, List<Users> users) {
        super(context, resource, users);
        this.context = context;
        this.resource = resource;
        this.usersList = users;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(resource, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.txt_TenNhanVien = convertView.findViewById(R.id.txt_TenNhanVien);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Users useritem = usersList.get(position);
        if (useritem != null) {
            viewHolder.txt_TenNhanVien.setText(useritem.getName());
        }

        return convertView;
    }

    static class ViewHolder {
        TextView txt_TenNhanVien;
    }
    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_name_user, parent, false);
        }

        TextView txt_TenNhanVien = convertView.findViewById(R.id.txt_TenNhanVien);
        Users useritem = this.getItem(position);

        if (useritem != null) {
            txt_TenNhanVien.setText(useritem.getName());
        }

        return convertView;
    }

}
