import React, { memo } from 'react';
import { Search, X } from 'lucide-react';

interface SearchBoxProps {
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  className?: string;
}

export const SearchBox: React.FC<SearchBoxProps> = memo(({
  value,
  onChange,
  placeholder = 'Search documents, departments...',
  className = '',
}) => {
  return (
    <div className={`relative flex items-center ${className}`}>
      <Search aria-hidden="true" className="absolute left-3 w-4 h-4 text-slate-400 pointer-events-none" />
      <input
        type="text"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder}
        aria-label={placeholder}
        className="w-full pl-9 pr-8 py-2 bg-slate-900/90 border border-slate-800 rounded-xl text-xs text-slate-200 placeholder:text-slate-500 focus:outline-none focus:border-indigo-500/80 focus:ring-1 focus:ring-indigo-500/40 transition-all shadow-inner"
      />
      {value && (
        <button
          type="button"
          onClick={() => onChange('')}
          aria-label="Clear document search filter"
          className="absolute right-2.5 p-1 rounded-md text-slate-400 hover:text-slate-200 hover:bg-slate-800 transition-colors cursor-pointer"
          title="Clear search"
        >
          <X className="w-3.5 h-3.5" />
        </button>
      )}
    </div>
  );
});

SearchBox.displayName = 'SearchBox';
