package music.playlist.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import music.playlist.reproducoes.ReproducaoEntity;

/**
 * Acesso ao microserviço reproducoes a partir do microserviço api.
 */
@FeignClient(name = "reproducoes", url = "${url}")
public interface ReproducaoClienteFeign {

    @PostMapping("/statistic")
    ReproducaoEntity registrar(@RequestBody ReproducaoEntity reproducao);

}
