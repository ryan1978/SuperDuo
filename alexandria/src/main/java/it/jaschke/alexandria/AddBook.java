package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;
import it.jaschke.alexandria.services.DownloadImage;


public class AddBook extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = AddBook.class.getSimpleName();

    private static final int LOADER_ID  = 1;
    private final String EAN_CONTENT    = "eanContent";

    private TextView    mErrorMessage;
    private ViewGroup   mMainLayout;
    private EditText    mEanText;
    private Button      mScanButton;
    private TextView    mBookTitle;
    private TextView    mBookSubtitle;
    private TextView    mBookAuthors;
    private ImageView   mBookCover;
    private TextView    mBookCategories;
    private Button      mDeleteButton;
    private Button      mSaveButton;

    public AddBook(){ }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mEanText !=null) {
            outState.putString(EAN_CONTENT, mEanText.getText().toString());
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView       = inflater.inflate(R.layout.fragment_add_book, container, false);
        mErrorMessage       = (TextView) rootView.findViewById(R.id.error_message);
        mMainLayout         = (ViewGroup) rootView.findViewById(R.id.main_layout);
        mScanButton         = (Button) rootView.findViewById(R.id.scan_button);
        mEanText            = (EditText) rootView.findViewById(R.id.ean);
        mBookTitle          = (TextView) rootView.findViewById(R.id.bookTitle);
        mBookSubtitle       = (TextView) rootView.findViewById(R.id.bookSubTitle);
        mBookAuthors        = (TextView) rootView.findViewById(R.id.authors);
        mBookCover          = (ImageView) rootView.findViewById(R.id.bookCover);
        mBookCategories     = (TextView) rootView.findViewById(R.id.categories);
        mDeleteButton       = (Button) rootView.findViewById(R.id.delete_button);
        mSaveButton         = (Button) rootView.findViewById(R.id.save_button);

        mEanText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                String ean = s.toString();

                //catch isbn10 numbers
                if (ean.length() == 10 && !ean.startsWith("978")) {
                    ean = "978" + ean;
                }

                if (ean.length() < 13) {
                    clearFields();
                    return;
                }

                if (Utility.isNetworkAvailable(getActivity())) {
                    //Once we have an ISBN, start a book intent
                    Intent bookIntent = new Intent(getActivity(), BookService.class);
                    bookIntent.putExtra(BookService.EAN, ean);
                    bookIntent.setAction(BookService.FETCH_BOOK);
                    getActivity().startService(bookIntent);
                    AddBook.this.restartLoader();
                } else {
                    mErrorMessage.setVisibility(View.VISIBLE);
                    mMainLayout.setVisibility(View.GONE);
                }
            }
        });

        mScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setClassName(BarcodeScannerActivity.class.getPackage().getName(), BarcodeScannerActivity.class.getName());
                startActivityForResult(i, BarcodeScannerActivity.RC_SCAN_BARCODE);
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEanText.setText("");
            }
        });

        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, mEanText.getText().toString());
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                mEanText.setText("");
            }
        });

        if(savedInstanceState != null){
            mEanText.setText(savedInstanceState.getString(EAN_CONTENT));
            mEanText.setHint("");
        }

        return rootView;
    }

    private void restartLoader(){
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(mEanText.getText().length()==0){
            return null;
        }
        String eanStr= mEanText.getText().toString();
        if(eanStr.length()==10 && !eanStr.startsWith("978")){
            eanStr="978"+eanStr;
        }
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(eanStr)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            String bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
            mBookTitle.setText(bookTitle);

            String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
            mBookSubtitle.setText(bookSubTitle);

            // TODO: FIX THIS! I've seen at least one book crash the app here when the authors returned were null
            String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
            if (authors != null) {
                String[] authorsArr = authors.split(",");
                mBookAuthors.setLines(authorsArr.length);
                mBookAuthors.setText(authors.replace(",", "\n"));
            }

            String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
            if(Patterns.WEB_URL.matcher(imgUrl).matches()){
                new DownloadImage(mBookCover).execute(imgUrl);
                mBookCover.setVisibility(View.VISIBLE);
                mBookCover.setContentDescription(bookTitle);
            }

            String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
            mBookCategories.setText(categories);

            mSaveButton.setVisibility(View.VISIBLE);
            mDeleteButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    private void clearFields(){
        mBookTitle.setText("");
        mBookSubtitle.setText("");
        mBookAuthors.setText("");
        mBookCategories.setText("");
        mBookCover.setVisibility(View.INVISIBLE);
        mBookCover.setContentDescription("");
        mSaveButton.setVisibility(View.INVISIBLE);
        mDeleteButton.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        getActivity().setTitle(R.string.scan);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case BarcodeScannerActivity.RC_SCAN_BARCODE:
                if (resultCode == Activity.RESULT_OK) {
                    Bundle res      = data.getExtras();
                    String barcode  = res.getString(BarcodeScannerActivity.EXTRA_BARCODE);
                    mEanText.setText(barcode);
                }
                break;
        }
    }

    private void updateErrorMessage() {
        if (!Utility.isNetworkAvailable(getActivity())) {
            mMainLayout.setVisibility(View.GONE);
            mErrorMessage.setText(R.string.no_internet_connection);
            mErrorMessage.setVisibility(View.VISIBLE);
        } else {
            mErrorMessage.setVisibility(View.GONE);
            mMainLayout.setVisibility(View.VISIBLE);
        }
    }
}
