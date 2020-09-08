package com.example.android.bluetoothadvertisements;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class AdvertiseButtonFragment extends Fragment implements View.OnClickListener {
    View view;
Button button;
    private static final String TAG = AdvertiserService.class.getSimpleName();
    public AdvertiseButtonFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view= inflater.inflate(R.layout.fragment_advetise_button, container, false);
        button=(Button)view.findViewById(R.id.btn);
        button.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.btn) {
            Intent intent = new Intent(this.getActivity(), AdvertiserService.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            getActivity().startActivityIfNeeded(intent,0);
            Log.i(TAG,"btn???");

        }
            else
            Toast.makeText(getActivity(), "Dafa ho", Toast.LENGTH_SHORT).show();
    }
}

