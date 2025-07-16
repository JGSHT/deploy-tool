package org.deploy.tool.flyway.http;

import java.io.InputStream;

import org.springframework.aot.hint.annotation.Reflective;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.Part;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PutExchange;

@Reflective
@HttpExchange("/deploy")
public interface ArtifactoryExchange {

  @GetExchange("/{fileName}")
  Resource pull(@PathVariable("fileName") String fileName);

  @PutExchange(value = "/{fileName}", contentType = MediaType.MULTIPART_FORM_DATA_VALUE)
  void push(@PathVariable("fileName") String fileName,
      @RequestPart(name = "file") Resource multipartFile);
}
