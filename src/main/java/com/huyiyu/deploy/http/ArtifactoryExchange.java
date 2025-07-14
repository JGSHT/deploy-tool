package com.huyiyu.deploy.http;

import org.springframework.aot.hint.annotation.Reflective;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@Reflective
@HttpExchange
public interface ArtifactoryExchange {

  @GetExchange
  Resource pull(@PathVariable("/{version}") String version);
}
