package training.edu.droidbountyhunter;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import training.edu.data.DBProvider;
import training.edu.fragment.About;
import training.edu.models.Fugitivo;

public class Home extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private Fragment[] fragments;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this, Agregar.class);
                startActivity(intent);
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_agregar) {
            Intent intent = new Intent(this, Agregar.class);
            startActivityForResult(intent,0);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void UpdateLists(int index){
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(index);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UpdateLists(requestCode);
    }

    /**
     * Fragment multiuso para mostrar la lista de Fugitivos o Capturados acorde
     * al argumento indicado.
     */
    public static class ListFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private static final String ARG_SECTION_TEXT = "section_text";

        public ListFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            // Se hace referencia al Fragment generado por XML en los Layouts y
            // se instancia en una View...
            View view = inflater.inflate(R.layout.fragment_list, container, false);

            Bundle args = this.getArguments();
            final int mode = args.getInt("mode");

            final ListView list = (ListView) view.findViewById(R.id.lista);
            UpdateList(list, mode);
            // Se genera el Listener para el detalle de cada elemento...
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ArrayList<Fugitivo> fugitivos = (ArrayList<Fugitivo>) list.getTag();
                    Fugitivo fugitivo = fugitivos.get(position);
                    Intent intent = new Intent(getContext(), Detalle.class);
                    intent.putExtra("title", fugitivo.getName());
                    intent.putExtra("mode", mode);
                    intent.putExtra("id", fugitivo.getId());
                    startActivityForResult(intent,mode);
                }
            });
            return view;
        }

        private void UpdateList(ListView list, int mode){
            DBProvider database = new DBProvider(getContext());
            ArrayList<Fugitivo> fugitivos = database.GetFugitivos(mode == 1);
            if (fugitivos.size() > 0){
                String[] data = new String[fugitivos.size()];
                for (int i = 0 ; i < fugitivos.size() ; i++){
                    data[i] = fugitivos.get(i).getName();
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                        android.R.layout.simple_list_item_1, data);
                list.setAdapter(adapter);
                list.setTag(fugitivos);
            }
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            fragments = new Fragment[3];
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a ListFragment (defined as a static inner class below).
            if (fragments[position] == null){
                if (position < 2){
                    fragments[position] = new ListFragment();
                    Bundle arguments = new Bundle();
                    arguments.putInt("mode",position);
                    fragments[position].setArguments(arguments);
                }else {
                    fragments[position] = new About();
                }
            }
            return fragments[position];
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.title_fugitivos);
                case 1:
                    return getString(R.string.title_capturados);
                case 2:
                    return getString(R.string.title_acercade);
            }
            return null;
        }
    }
}
