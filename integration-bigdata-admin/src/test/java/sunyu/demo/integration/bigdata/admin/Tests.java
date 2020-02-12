package sunyu.demo.integration.bigdata.admin;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class Tests {

    @Test
    public void t() {
        System.out.println(HttpUtil.post("http://openapi.qas.uml-tech.com/farm/getRealTime",
                "{'dids': 'NJHYBVHAS0000153','token': '2368f8455648f86b71e6e6a44c8d6a86'}"));
    }

    @Test
    public void t2() {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
            }
            if (new Random().nextInt() % 2 >= 0) {
                int i = 12 / 0;
            }
            System.out.println("run end ...");
        }).whenComplete((t, action) -> System.out.println("执行完成！")).exceptionally(t -> {
            System.out.println("执行失败！" + t.getMessage());
            return null;
        });
        future.join();
    }

    @Test
    public void t3() {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
            }
            if (new Random().nextInt() % 2 >= 0) {
                int i = 12 / 0;
            }
            System.out.println("run end ...");
        }).handle((aVoid, throwable) -> {
            System.out.println(aVoid);
            if (throwable != null) {
                System.out.println(throwable.getMessage());
            }
            return null;
        });
        future.join();
    }

    @Test
    public void t4() {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
            }
            if (new Random().nextInt() % 2 >= 0) {
                int i = 12 / 0;
            }
            System.out.println("run end ...");
            return 0;
        }).handle((integer, throwable) -> {
            System.out.println(integer);
            if (throwable != null) {
                System.out.println(throwable.getMessage());
            }
            return null;
        });
        future.join();
    }

    @Test
    public void t5() {
        System.out.println("a");
        System.out.println(NumberUtil.div(DateUtil.between(DateUtil.parse("0750", "HHmm"), DateUtil.parse("1816", "HHmm"), DateUnit.MINUTE), 60));
        System.out.println("b");
    }

    @Test
    public void t6() {
        String ql = "select   rowKey,did,3014,2205 from can_ne#can where 3014 != '' and startRowKey = '00004baa3388ab01e3d153347e7fc163_20191204132714' and 3014!='' limit 20";
        StringBuilder sql = new StringBuilder();
        boolean b = false;
        for (String s : ql.split(" ")) {
            if (StrUtil.isNotBlank(s)) {
                if (s.equals("startRowKey")) {
                    sql.append(s);
                    b = true;
                } else {
                    if (b && !s.equals("=")) {
                        sql.append("'aaa'");
                        b = false;
                    } else {
                        sql.append(s);
                    }
                }
                sql.append(" ");
            }
        }
        System.out.println(sql);
    }
}
