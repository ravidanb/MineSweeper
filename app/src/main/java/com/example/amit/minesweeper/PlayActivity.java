package com.example.amit.minesweeper;

import android.content.Intent;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;


public class PlayActivity extends AppCompatActivity implements Board.BoardListener {

    public static final int BIGGER_FRACTION = 6;
    public static final int SMALLER_FRACTION = 12;


    private int seconds = 0;

    private Board board;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        Button quit = (Button) findViewById(R.id.button_quit);

        quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(view.getContext(), EndGameActivity.class);
                intent.putExtra(Keys.RESULT, false);
                intent.putExtra(Keys.TIME, seconds);
                int cubes = board.getNumOfPressedBlocks();
                int goodFlags = board.getNumOfGoodFlags();
                intent.putExtra(Keys.GOOD_CUBES,cubes);
                intent.putExtra(Keys.GOOD_FLAGS,goodFlags);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                finish();

            }
        });
        board = buildBoard();
        tickEndlessly();

    }

    public Board buildBoard() {

        Bundle bundle = getIntent().getExtras();
        MainActivity.eDifficulty difficulty = (MainActivity.eDifficulty) bundle.getSerializable(Keys.DIFFICULTY);
        int numOfMines = bundle.getInt(Keys.NUM_OF_MINES);
        int boardSize = bundle.getInt(Keys.BOARD_SIZE);

        int buttonWidth = calculateButtonSize(difficulty);

        GridLayout gridLayout = (GridLayout) findViewById(R.id.grid);


        Board board = new Board(this, gridLayout, boardSize, buttonWidth, numOfMines);
        board.setBoardListener(this);
        return board;
    }


    public int calculateButtonSize(MainActivity.eDifficulty difficulty) {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        int fraction;

        if (difficulty.ordinal() <= MainActivity.eDifficulty.INTERMEDIATE.ordinal())
            fraction = SMALLER_FRACTION;
        else
            fraction = BIGGER_FRACTION;

        int theSmallerAxis = height < width ? height : width;
        return theSmallerAxis / fraction;
    }


    private void tickEndlessly() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    tick();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException exception) {
                        exception.printStackTrace();
                    }
                }
            }
        }).start();

    }

    private void tick() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView textTime = (TextView) findViewById(R.id.timer);
                textTime.setText(getString(R.string.play_activity_time) + " " + seconds);
                board.updateSecond(seconds++);
            }
        });

    }

    @Override
    public void onUpdate(int numOfPressedBlocks, int numOfFlags) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                TextView flagsOnPlay = (TextView) findViewById(R.id.flags);
                TextView CubesOnPlay = (TextView) findViewById(R.id.score);
                flagsOnPlay.setText(getString(R.string.flags) + " " + board.getNumOfFlags());
                CubesOnPlay.setText(getString(R.string.score) + " " + board.getNumOfPressedBlocks());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        board.setBoardListener(null); // clear reference to the PlayActivity for garbage collector to clean
    }
}
