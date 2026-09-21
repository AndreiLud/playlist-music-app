package music.playlist.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import music.playlist.musicas.MusicaEntity;

/**
 * Acesso ao microserviço musicas a partir do microserviço api.
 */
@FeignClient(name = "musicas", url = "${url}")
public interface MusicaClienteFeign {

    @GetMapping("/musicas/{id}")
    MusicaEntity obterMusica(@PathVariable("id") Integer id);

}
