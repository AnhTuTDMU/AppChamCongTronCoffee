package Adapter;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.nghiencuukhoahoc_appchamcong.R;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import DTO.Users;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends ArrayAdapter<Users> {
    private Context context;
    private int resource;
    private List<Users> usersList;
    public UserAdapter(Context context, int resource, List<Users> users) {
        super(context, resource, users);
        this.context = context;
        this.resource = resource;
        this.usersList = users;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(resource, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.txt_TenNhanVien = convertView.findViewById(R.id.txt_TenNhanVien);
            viewHolder.img_ChonAnh = convertView.findViewById(R.id.img_ChonAnh);
            viewHolder.txt_NgaySinh = convertView.findViewById(R.id.txt_NgaySinh);
            viewHolder.txt_GioiTinh = convertView.findViewById(R.id.txt_GioiTinh);
            viewHolder.txt_SoDienThoai = convertView.findViewById(R.id.txt_SoDienThoai);
            viewHolder.txt_Email = convertView.findViewById(R.id.txt_Email);
            viewHolder.txt_LuongCoBan = convertView.findViewById(R.id.txt_LuongCoBan);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Users useritem = usersList.get(position);
        if (useritem != null) {
            viewHolder.txt_TenNhanVien.setText(useritem.getName());
            if (viewHolder.img_ChonAnh != null && useritem.getImg() != null && !useritem.getImg().isEmpty()) {
                Picasso.get().load(useritem.getImg()).placeholder(R.drawable.icons8user64).into(viewHolder.img_ChonAnh);
            }


            long ngaySinhLong = useritem.getDateOfBirth();
            if (ngaySinhLong > 0) {
                Date ngaySinhDate = new Date(ngaySinhLong);
                SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String ngaySinhFormatted = displayFormat.format(ngaySinhDate);
                viewHolder.txt_NgaySinh.setText(ngaySinhFormatted);
            } else {
                viewHolder.txt_NgaySinh.setText("Ngày sinh không có");
            }

            viewHolder.txt_GioiTinh.setText(String.valueOf(useritem.getSex()));
            viewHolder.txt_SoDienThoai.setText(String.valueOf(useritem.getPhoneNumber()));
            viewHolder.txt_Email.setText(String.valueOf(useritem.getEmail()));

            String luongCoBanFormatted = String.format(Locale.getDefault(), "%,.0f VNĐ", useritem.getSalary());
            viewHolder.txt_LuongCoBan.setText(luongCoBanFormatted);
        }

        return convertView;
    }

    static class ViewHolder {
        TextView txt_TenNhanVien;
        CircleImageView img_ChonAnh;
        TextView txt_NgaySinh;
        TextView txt_GioiTinh;
        TextView txt_SoDienThoai;
        TextView txt_Email;
        TextView txt_LuongCoBan;
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
