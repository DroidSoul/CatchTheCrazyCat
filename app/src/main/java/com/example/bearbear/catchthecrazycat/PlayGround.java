package com.example.bearbear.catchthecrazycat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bear&bear on 7/4/2016.
 */
public class PlayGround extends SurfaceView implements View.OnTouchListener{

    private static int WIDTH = 80;
    private static final int ROW = 10;
    private static final int COL = 10;
    private static final int BLOCKS = 15; //number of initial blocks
    private Dot[][] matrix;
    private Dot cat;

    public PlayGround(Context context) {
        super(context);
        getHolder().addCallback(callback);
        matrix = new Dot[ROW][COL];
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COL; j++) {
                matrix[i][j] = new Dot(j, i);
            }
        }
        setOnTouchListener(this);
        initGame();
    }
    private Dot getDot(int x, int y) {
        return matrix[y][x];
    }

    private boolean isAtEdge(Dot d) {
        if (d.getX()*d.getY() == 0 || d.getX() == COL - 1 || d.getY() == ROW - 1) {
            return true;
        }
        return false;
    }

    private Dot getNeighbor(Dot one, int dir) {
        switch (dir) {
            case 1:
                return getDot(one.getX() - 1, one.getY());
            case 2:
                if (one.getY() % 2 == 0) {
                    return getDot(one.getX() - 1, one.getY() - 1);
                }else {
                    return getDot(one.getX(), one.getY() - 1);
                }
            case 3:
                if (one.getY() % 2 == 0) {
                    return getDot(one.getX(), one.getY() - 1);
                }else {
                    return getDot(one.getX() + 1, one.getY() - 1);
                }
            case 4:
                return getDot(one.getX() + 1, one.getY());
            case 5:
                if (one.getY() % 2 == 0) {
                    return getDot(one.getX(), one.getY() + 1);
                }else {
                    return getDot(one.getX() + 1, one.getY() + 1);
                }
            case 6:
                if (one.getY() % 2 == 0) {
                    return getDot(one.getX() - 1, one.getY() + 1);
                }else {
                    return getDot(one.getX(), one.getY() + 1);
                }
        }
        return null;
    }

    private int getDistance(Dot one, int dir) {
        int distance = 0;
        if (isAtEdge(one)) {
            return 1;
        }
        Dot ori = one, next;
        while (true) {
            next = getNeighbor(ori, dir);
            if (next.getStatus() == Dot.STATUS_ON) {
                return distance * -1;
            }
            if (isAtEdge(next)) {
                return distance+1;
            }
            ori = next;
            distance++;
        }
    }

    private void moveTo(Dot one) {
        one.setStatus(Dot.STATUS_IN);
        getDot(cat.getX(), cat.getY()).setStatus(Dot.STATUS_OFF);
        cat.setXY(one.getX(), one.getY());
    }

    private void move() {
        if (isAtEdge(cat)) {
            lose();
            return;
        }
        List<Dot> available = new ArrayList<>();
        List<Dot> positive = new ArrayList<>();
        Map<Dot, Integer> map = new HashMap<>();
        for (int i = 1; i <= 6; i++) {
            Dot n = getNeighbor(cat, i);
            if (n.getStatus() == Dot.STATUS_OFF) {
                available.add(n);
                map.put(n, i);
                if (getDistance(n, i) > 0) {
                    positive.add(n);
                }
            }
        }
        if (available.size() == 0) {
            win();
            return;
        }
        else if (available.size() == 1){
            moveTo(available.get(0));
        } else {
            Dot best = null;
            if (positive.size() > 0) {
                int min = Integer.MAX_VALUE;
                for (Dot pos : positive) {
                    int dis = getDistance(pos, map.get(pos));
                    if (dis < min) {
                        min = dis;
                        best = pos;
                    }
                }
            }
            else {
                int max = 1;
                for (Dot pos : available) {
                    int dis = getDistance(pos, map.get(pos));
                    if (dis < max) {
                        max = dis;
                        best = pos;
                    }
                }
            }
            moveTo(best);
        }
    }

    private void lose() {
        Toast.makeText(getContext(), "You lose!", Toast.LENGTH_SHORT).show();
        initGame();
    }

    private void win() {
        Toast.makeText(getContext(), "You win!", Toast.LENGTH_SHORT).show();
        initGame();
    }


    public void redraw() {
        Canvas c = getHolder().lockCanvas();
        c.drawColor(Color.LTGRAY);
        Paint paint = new Paint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        for (int i = 0; i < ROW; i++) {
            int offset = 0;
            if (i % 2 != 0) {
                offset = WIDTH / 2;
            }
            for (int j = 0; j < COL; j++) {
                Dot temp = getDot(j, i);
                switch (temp.getStatus()) {
                    case Dot.STATUS_OFF:
                        paint.setColor(0xFFEEEEEE);
                        break;
                    case Dot.STATUS_ON:
                        paint.setColor(0xFFFFAA00);
                        break;
                    case Dot.STATUS_IN:
                        paint.setColor(0xFFFF0000);
                        break;
                }
                c.drawOval(new RectF(temp.getX() * WIDTH + offset, temp.getY() * WIDTH, (temp.getX() + 1) * WIDTH + offset, (temp.getY() + 1) * WIDTH), paint);
            }
        }
        getHolder().unlockCanvasAndPost(c);
    }

    SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            redraw();
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            WIDTH = i1 / (COL + 1);
            redraw();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

        }
    };

    private void initGame() {
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COL; j++) {
                matrix[i][j].setStatus(Dot.STATUS_OFF);
            }
        }
        cat = new Dot(4, 5);
        getDot(4, 5).setStatus(Dot.STATUS_IN);
        for (int i = 0; i < BLOCKS;) {
            int x = (int) (Math.random() * COL);
            int y = (int) (Math.random() * ROW);
            if (getDot(x, y).getStatus() == Dot.STATUS_OFF) {
                getDot(x, y).setStatus(Dot.STATUS_ON);
                i++;
            }
        }
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
//            Toast.makeText(getContext(), motionEvent.getX() + ":" + motionEvent.getY(), Toast.LENGTH_SHORT).show();
            int x, y;
            y = (int) (motionEvent.getY() / WIDTH);
            if (y % 2 == 0) {
                x = (int) (motionEvent.getX() / WIDTH);

            }else {
                x = (int) ((motionEvent.getX() - WIDTH / 2) / WIDTH);
            }
            if (x >= 0 && x < ROW && y >= 0 && y < COL && getDot(x, y).getStatus() == Dot.STATUS_OFF) {
                getDot(x, y).setStatus(Dot.STATUS_ON);
                move();
                redraw();
            }
        }
        return true;
    }
}
