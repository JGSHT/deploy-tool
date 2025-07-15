package com.huyiyu.deploy.http;

import java.io.InputStream;
import org.springframework.aot.hint.annotation.Reflective;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PutExchange;

@Reflective
@HttpExchange("/deploy")
public interface ArtifactoryExchange {

  @GetExchange("/{fileName}")
  Resource pull(@PathVariable("/{fileName}") String fileName);

  @PutExchange("/{fileName}")
  void push(@PathVariable("/{fileName}") String fileName, InputStream inputStream);
}
