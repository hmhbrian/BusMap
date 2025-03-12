package com.example.busmap.Route.FindRoute;

import static com.example.busmap.FindRouteHelper.Tranfers.formatDistance;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.busmap.FindRouteHelper.LocationData;
import com.example.busmap.R;
import com.example.busmap.FindRouteHelper.instruction;
import com.example.busmap.entities.station;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RouteInstructionAdapter extends RecyclerView.Adapter<RouteInstructionAdapter.ViewHolder> {
    private Context context;
    private Map<String, List<station>> stationOfRoute;
    private LatLng MyLocation;
    private List<instruction> instructions;
    private Map<String, Integer> priceMap;
    private LocationData destination;


    public RouteInstructionAdapter(Context context, Map<String, List<station>> stationOfRoute, LatLng startLocation,LocationData to, Map<String, Integer> priceMap)
    {
        this.context = context;
        this.stationOfRoute = stationOfRoute;
        this.MyLocation = startLocation;
        this.destination = to;
        this.instructions = generateInstructions();
        this.priceMap = priceMap;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_instruction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String tv_instruct = instructions.get(position).getStep().toString();
        holder.tv_instruction.setText(tv_instruct);

        if (tv_instruct.startsWith("Đi Tuyến")) {
            holder.img_icon.setImageResource(R.drawable.baseline_directions_bus_24);
            holder.edt_price.setVisibility(View.VISIBLE);
            String[] parts = tv_instruct.split(" ");
            if (parts.length > 2) {
                String routeName = parts[1] + " " + parts[2];
                if (priceMap.containsKey(routeName)) {
                    int price = priceMap.get(routeName);
                    holder.edt_price.setText(price+" đ");
                }
            }
        }else{
            holder.img_icon.setImageResource(R.drawable.human);
        }

        if(instructions.get(position).getDistance().isEmpty()){
            holder.tv_distance.setVisibility(View.GONE);
            holder.tv_time.setVisibility(View.GONE);
        }else{
            holder.tv_time.setText(instructions.get(position).getTimeEstimate());
            holder.tv_distance.setText(instructions.get(position).getDistance());
        }
    }

    @Override
    public int getItemCount() {
        return instructions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_instruction,tv_time,tv_distance;
        ImageView img_icon;
        EditText edt_price;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_instruction = itemView.findViewById(R.id.tv_instruction);
            img_icon = itemView.findViewById(R.id.img_icon);
            edt_price = itemView.findViewById(R.id.edt_price);
            tv_distance = itemView.findViewById(R.id.tv_distance);
            tv_time = itemView.findViewById(R.id.tv_time);
        }
    }

    private List<instruction> generateInstructions() {
        List<instruction> steps = new ArrayList<>();
        List<String> routeNames = new ArrayList<>(stationOfRoute.keySet());

        int routeCount = routeNames.size();

        if (routeCount == 0) {
            steps.add(new instruction(makeBoldText("Không tìm thấy tuyến đường!",""),"",""));
            return steps;
        }
        double distance1, distance2, distance3;
        // Trường hợp chỉ có 1 tuyến (không có trung chuyển)
        if (routeCount == 1) {
            String route = routeNames.get(0);
            List<station> stations = stationOfRoute.get(route);
            if (stations == null || stations.isEmpty()) {
                steps.add(new instruction(makeBoldText("Dữ liệu tuyến không hợp lệ!",""),"",""));
                return steps;
            }
            distance1 = calculateDistance(MyLocation.latitude,MyLocation.longitude,stations.get(0).getLat(),stations.get(0).getLng());
            steps.add(new instruction(makeBoldText("Đi đến trạm ",stations.get(0).getName()), formatDistance(distance1), estimateTime(distance1,"walking")));
            distance2 = calculateTotalDistance(stations);
            steps.add(new instruction(makeBoldText("Đi ",route), formatDistance(distance2), estimateTime(distance2,"bus")));
            if(destination.getName().equals("[ Tọa độ điểm ]")){
                distance3 = calculateDistance(stations.get(stations.size() - 1).getLat(),stations.get(stations.size() - 1).getLng(),destination.getLatitude(),destination.getLongitude());
                steps.add(new instruction(makeBoldPart("Xuống tại trạm ",stations.get(stations.size() - 1).getName()," và đi đến điểm đến."),formatDistance(distance3),estimateTime(distance3,"walking")));
            }else
                steps.add(new instruction(makeBoldPart("Xuống tại trạm ",stations.get(stations.size() - 1).getName()," và đi đến điểm đến."),"",""));

            return steps;
        }

        // Trường hợp có 2 hoặc 3 tuyến (tuyến trung chuyển)
        List<station> firstRouteStations = stationOfRoute.get(routeNames.get(0));
        List<station> secondRouteStations = routeCount > 1 ? stationOfRoute.get(routeNames.get(1)) : null;
        List<station> thirdRouteStations = routeCount > 2 ? stationOfRoute.get(routeNames.get(2)) : null;

        if (firstRouteStations == null || secondRouteStations == null) {
            steps.add(new instruction(makeBoldText("Dữ liệu tuyến không hợp lệ!",""),"",""));
            return steps;
        }

        // Tìm trạm trung chuyển đầu tiên
        station firstTransferStation = findTransferStation(firstRouteStations, secondRouteStations);
        if (firstTransferStation == null) {
            steps.add(new instruction(makeBoldText("Không tìm thấy trạm trung chuyển!",""),"",""));
            return steps;
        }

        // Hướng dẫn đến trạm đầu tiên của tuyến đầu
        station firstStation = firstRouteStations.get(0);
        distance1 = calculateDistance(MyLocation.latitude,MyLocation.longitude,firstStation.getLat(),firstStation.getLng());
        steps.add(new instruction(makeBoldText("Đi đến trạm ",firstStation.getName()), formatDistance(distance1), estimateTime(distance1,"walking")));


        // Đi tuyến đầu tiên
        distance2 = calculateTotalDistance(firstRouteStations);
        steps.add(new instruction(makeBoldText("Đi ",routeNames.get(0)), formatDistance(distance2), estimateTime(distance2,"bus")));


        // Xuống tại trạm trung chuyển đầu tiên
        steps.add(new instruction(makeBoldPart("Xuống tại trạm ",firstTransferStation.getName(),""),"",""));

        // Nếu chỉ có 2 tuyến, đi tuyến thứ 2 và kết thúc
        if (routeCount == 2 || thirdRouteStations == null) {
            distance3 = calculateTotalDistance(secondRouteStations);
            steps.add(new instruction(makeBoldText("Đi ",routeNames.get(1)), formatDistance(distance3), estimateTime(distance3,"bus")));


            station lastStation = secondRouteStations.get(secondRouteStations.size() - 1);
            if(destination.getName().equals("[ Tọa độ điểm ]")){
                double distance4 = calculateDistance(lastStation.getLat(),lastStation.getLng(),destination.getLatitude(),destination.getLongitude());
                steps.add(new instruction(makeBoldPart("Xuống tại trạm ",lastStation.getName()," và đi đến điểm đến."),formatDistance(distance4),estimateTime(distance4,"walking")));
            }else
                steps.add(new instruction(makeBoldPart("Xuống tại trạm ",lastStation.getName()," và đi đến điểm đến."),"",""));
            return steps;
        }

        // Nếu có 3 tuyến, tìm trạm trung chuyển thứ hai
        station secondTransferStation = findTransferStation(secondRouteStations, thirdRouteStations);
        if (secondTransferStation == null) {
            steps.add(new instruction(makeBoldText("Không tìm thấy trạm trung chuyển thứ hai!",""),"",""));
            return steps;
        }

        // Đi tuyến thứ 2
        distance3 = calculateTotalDistance(secondRouteStations);
        steps.add(new instruction(makeBoldText("Đi ",routeNames.get(1)), formatDistance(distance3), estimateTime(distance3,"bus")));


        // Xuống tại trạm trung chuyển thứ hai
        steps.add(new instruction(makeBoldPart("Xuống tại trạm ",secondTransferStation.getName(),""),"",""));

        // Đi tuyến thứ 3
        double distance4 = calculateTotalDistance(thirdRouteStations);
        steps.add(new instruction(makeBoldText("Đi ",routeNames.get(2)), formatDistance(distance4), estimateTime(distance4,"bus")));

        // Xuống tại trạm cuối cùng
        station finalStation = thirdRouteStations.get(thirdRouteStations.size() - 1);
        if(destination.getName().equals("[ Tọa độ điểm ]")){
            double distance5 = calculateDistance(finalStation.getLat(),finalStation.getLng(),destination.getLatitude(),destination.getLongitude());
            steps.add(new instruction(makeBoldPart("Xuống tại trạm ",finalStation.getName()," và đi đến điểm đến."),formatDistance(distance4),estimateTime(distance4,"walking")));
        }else
            steps.add(new instruction(makeBoldPart("Xuống tại trạm ",finalStation.getName()," và đi đến điểm đến."),"",""));

        return steps;
    }

    private station findTransferStation(List<station> firstRoute, List<station> secondRoute) {
        Set<String> stationSet = new HashSet<>();
        for (station station : firstRoute) {
            stationSet.add(station.getName());
        }
        for (station station : secondRoute) {
            if (stationSet.contains(station.getName())) {
                return station;
            }
        }
        return null;
    }

    private SpannableString makeBoldText(String normalText, String boldText) {
        SpannableString spannable = new SpannableString(normalText + boldText);
        spannable.setSpan(new StyleSpan(Typeface.BOLD), normalText.length(), spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }
    private SpannableString makeBoldPart(String before, String bold, String after) {
        SpannableString spannable = new SpannableString(before + bold + after);
        spannable.setSpan(new StyleSpan(Typeface.BOLD), before.length(), before.length() + bold.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Bán kính Trái đất (km)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Khoảng cách (km)
    }

    private String estimateTime(double distance, String type) {
        double speed = type.equals("bus") ? 30.0 : 20.0; // km/h (bus hoặc đi bộ)
        double timeInMinutes = (distance / speed) * 60;
        return String.format("%.0f phút", timeInMinutes);
    }

    private double calculateTotalDistance(List<station> stations) {
        double totalDistance = 0.0;

        for (int i = 0; i < stations.size() - 1; i++) {
            station currentStation = stations.get(i);
            station nextStation = stations.get(i + 1);

            double distance = calculateDistance(
                    currentStation.getLat(), currentStation.getLng(),
                    nextStation.getLat(), nextStation.getLng()
            );

            totalDistance += distance;
        }

        return totalDistance;
    }

}

