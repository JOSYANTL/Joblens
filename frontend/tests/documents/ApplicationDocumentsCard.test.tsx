import { describe, expect, it } from 'vitest';
import { validateDocumentFile } from '../../src/features/documents/ApplicationDocumentsCard';

describe('ApplicationDocumentsCard', () => {
  it('validates the selected file before upload', () => {
    expect(validateDocumentFile(null)).toBe('请选择要上传的文件。');
    expect(validateDocumentFile(new File(['text'], 'notes.txt'))).toBe('只支持 PDF 和 DOCX 文件。');
    expect(validateDocumentFile(new File(['%PDF'], 'resume.pdf'))).toBeNull();
  });
});
