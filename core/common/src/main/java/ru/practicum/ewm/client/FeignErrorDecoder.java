package ru.practicum.ewm.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.exception.ApiException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.handler.ErrorResponse;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class FeignErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultDecoder = new Default();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            String body = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
            ErrorResponse error = mapper.readValue(body, ErrorResponse.class);
            HttpStatus status = HttpStatus.valueOf(response.status());

            if (status.is4xxClientError()) {
                if (status == HttpStatus.NOT_FOUND) {
                    return new NotFoundException(error.getMessage());
                }
                if (status == HttpStatus.CONFLICT) {
                    return new ConflictException(error.getMessage());
                }
                if (status == HttpStatus.BAD_REQUEST) {
                    return new ValidationException(error.getMessage());
                }
                return new ApiException(error.getReason(), error.getMessage(), status);
            }

            return defaultDecoder.decode(methodKey, response);

        } catch (Exception e) {

            log.warn("Failed to decode Feign error response for method {}. Falling back to default.", methodKey, e);

            return defaultDecoder.decode(methodKey, response);
        }
    }
}
