package diana.dev.order_service.service;

import diana.dev.order_service.dto.OrderResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class OrderNotificationService {

    // список всех открытых браузеров (подписчиков)
    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter createConnection() {

        SseEmitter emitter = new SseEmitter(24*60*60*100L);
        this.emitters.add(emitter);

        emitter.onCompletion(() -> this.emitters.remove(emitter));
        emitter.onTimeout(() -> this.emitters.remove(emitter));
        emitter.onError((e) -> this.emitters.remove(emitter));

        return emitter;
    }

    public void sendOrderUpdate(OrderResponse updatedOrder) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("order-update")
                        .data(updatedOrder));
            } catch (Exception e) {
                emitters.remove(emitter);
            }
        }
    }

}
