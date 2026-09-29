package com.josyantl.joblens.document.application;

import com.josyantl.joblens.document.domain.model.ApplicationDocument;

public record DownloadedDocument(ApplicationDocument metadata, byte[] content) {}
