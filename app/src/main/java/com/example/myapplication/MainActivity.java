package com.example.myapplication;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private boolean animationStarted = false; // 防止动画重复触发

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 处理系统窗口Insets（适配状态栏/导航栏）
        androidx.constraintlayout.widget.ConstraintLayout rootLayout = findViewById(R.id.main);
        if (rootLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }

    /**
     * 在窗口焦点获取后再执行动画，确保 View 初始化完毕
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && !animationStarted) {
            animationStarted = true;
            try {
                startAllAnimations();
            } catch (Exception e) {
                Log.e("MainActivity", "动画启动异常", e);
                Toast.makeText(this, "动画加载出错，请检查资源", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startAllAnimations() {
        ConfettiView konfettiView = findViewById(R.id.konfettiView);
        ImageView ivSuccessCheck = findViewById(R.id.ivSuccessCheck);

        // 1. 播放成功图标弹跳动画
        if (ivSuccessCheck != null) {
            // 确保初始不可见
            ivSuccessCheck.setAlpha(0f);
            ivSuccessCheck.setScaleX(0f);
            ivSuccessCheck.setScaleY(0f);

            ivSuccessCheck.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(500)
                    .setStartDelay(100)
                    .setInterpolator(new OvershootInterpolator(1.2f))
                    .start();
        }

        // 2. 播放自定义SVG爆炸效果
        if (konfettiView != null) {
            explodeConfettiWithSvg(konfettiView);
        }
    }

    /**
     * 核心方法：使用12个SVG图形实现爆炸效果
     */
    private void explodeConfettiWithSvg(ConfettiView view) {
        // 1. 定义颜色
        List<Integer> colors = Arrays.asList(
                0xFFFF5225, 0xFFFC379E, 0xFFFFB53E, 0xFF3E8EFF,
                0xFFFFB53E, 0xFFFF5826, 0xFF39C5EE, 0xFF3AC6F4,
                0xFF50DFB2, 0xFFFF5826, 0xFF3DC4EF, 0xFFFFB53E
        );

        // 2. 加载所有12个SVG图形资源
        List<Drawable> svgShapes = loadAllSvgShapes();
        if (svgShapes.isEmpty()) {
            Log.w("MainActivity", "未加载到SVG资源，跳过爆炸效果");
            return;
        }

        // 3. 执行爆炸动画
        // 这里的 X 和 Y 是相对于 View 的坐标，这里简单取中心点偏上的位置
        view.post(() -> {
            float x = view.getWidth() * 0.5f;
            float y = view.getHeight() * 0.35f;
            view.explode(svgShapes, colors, x, y);
        });
    }

    /**
     * 加载资源逻辑
     */
    private List<Drawable> loadAllSvgShapes() {
        List<Drawable> shapeList = new ArrayList<>();

        int[] drawableIds = {
                R.drawable.explosion_piece1, R.drawable.explosion_piece2,
                R.drawable.explosion_piece3, R.drawable.explosion_piece4,
                R.drawable.explosion_piece5, R.drawable.explosion_piece6,
                R.drawable.explosion_piece7, R.drawable.explosion_piece8,
                R.drawable.explosion_piece9, R.drawable.explosion_piece10,
                R.drawable.explosion_piece11, R.drawable.explosion_piece12
        };

        for (int id : drawableIds) {
            try {
                Drawable drawable = ContextCompat.getDrawable(this, id);
                if (drawable != null) {
                    shapeList.add(drawable);
                }
            } catch (Exception e) {
                Log.e("MainActivity", "资源加载失败 ID: " + id, e);
            }
        }
        return shapeList;
    }
}
