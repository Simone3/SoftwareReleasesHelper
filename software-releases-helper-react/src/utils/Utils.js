
/**
 * Some misc utilities
 */
class Utils {
	
	static replacePlaceholders(sourceString, placeholderValues) {
		if(!sourceString) {
			return sourceString;
		}

		if(!placeholderValues || !placeholderValues.length) {
			return sourceString;
		}

		// Very simple replace algorithm because implementation must be the same between front-end (preview) and back-end (real computation), but they could be both evolved for better performance
		let result = sourceString;
		for(const placeholder of placeholderValues) {
			if(placeholder.value) {
				result = result.replaceAll(`#[${placeholder.key}]`, placeholder.value);
			}
		}
		return result;
	}

	static removeWhitespace(text) {
		return text
			.replaceAll(/\s+/g, '')
			.replaceAll(/\u200B/g, '');
	}

	static escapeHtml(text) {
		if(!text) {
			return text;
		}
		return text
			.replace(/&/g, '&amp;')
			.replace(/</g, '&lt;')
			.replace(/>/g, '&gt;')
			.replace(/"/g, '&quot;')
			.replace(/'/g, '&#39;');
	}

	static formatDate(date) {
		const day = String(date.getDate()).padStart(2, '0');
		const month = String(date.getMonth() + 1).padStart(2, '0');
		const year = date.getFullYear();
		const hours = String(date.getHours()).padStart(2, '0');
		const minutes = String(date.getMinutes()).padStart(2, '0');
		const seconds = String(date.getSeconds()).padStart(2, '0');
		return `${day}/${month}/${year} ${hours}:${minutes}:${seconds}`;
	}

	static formatHtmlText(text) {
		if(!text) {
			return text;
		}
		text = Utils.escapeHtml(text);
		return text
			.replaceAll('#NEW_LINE#', '<br/>')
			.replaceAll('#START_LIST#', '<ul>')
			.replaceAll('#END_LIST#', '</ul>')
			.replaceAll('#START_LIST_ELEMENT#', '<li>')
			.replaceAll('#END_LIST_ELEMENT#', '</li>');
	}
}

export default Utils;
