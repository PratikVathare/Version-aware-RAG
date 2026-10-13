package com.enterprise.rag.service;

import com.enterprise.rag.dto.RagQueryRequest;
import com.enterprise.rag.dto.RagQueryResponse;

public interface RagService {

    RagQueryResponse executeVersionAwareQuery(RagQueryRequest request);
}
