package org.lushen.mrh.id.generator.boot;

import org.lushen.mrh.id.generator.revision.RevisionIdGenerator;
import org.lushen.mrh.id.generator.segment.SegmentIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class IdGeneratorController {

	@Autowired
	private RevisionIdGenerator revisionIdGenerator;
	@Autowired
	private SegmentIdGenerator segmentIdGenerator;

	@GetMapping(path="revision")
	public long revision() {
		return revisionIdGenerator.generate();
	}

	@GetMapping(path="segment")
	public long segment() {
		return segmentIdGenerator.generate();
	}

}
