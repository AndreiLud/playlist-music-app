package music.playlist.excecoes;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({ MusicaNaoLocalizadaException.class, PlaylistNaoLocalizadaException.class })
    public ResponseEntity<Map<String, String>> handleNaoLocalizado(RuntimeException e) {
        return new ResponseEntity<Map<String, String>>(erro(e.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MusicaEmPlaylistException.class)
    public ResponseEntity<Map<String, String>> handleMusicaEmPlaylist(MusicaEmPlaylistException e) {
        return new ResponseEntity<Map<String, String>>(erro(e.getMessage()), HttpStatus.CONFLICT);
    }

    // erros das validacoes definidas nas entidades (@NotBlank, @Size, @Positive...)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<String, String>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return errors;
    }

    private Map<String, String> erro(String mensagem) {
        Map<String, String> corpo = new LinkedHashMap<String, String>();
        corpo.put("erro", mensagem);
        return corpo;
    }

}
